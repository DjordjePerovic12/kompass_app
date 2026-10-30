package llc.bokadev.kompass.presentation.essentials.utility_map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.essentials_couldnt_load_utilities
import kompass.composeapp.generated.resources.essentials_hide_list
import kompass.composeapp.generated.resources.essentials_navigate
import kompass.composeapp.generated.resources.essentials_navigate_to
import kompass.composeapp.generated.resources.essentials_no_utilities_found
import kompass.composeapp.generated.resources.essentials_recenter
import kompass.composeapp.generated.resources.essentials_selection_gone
import kompass.composeapp.generated.resources.essentials_show_list
import kompass.composeapp.generated.resources.essentials_tile_all_on_map
import kompass.composeapp.generated.resources.essentials_you_are_here
import kompass.composeapp.generated.resources.retry
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.essentials.utils.UtilityLocationItem
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.detail.DetailPillButton
import llc.bokadev.kompass.presentation.shared.NativeUtilityDetailMapView
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.cardTitleRes
import llc.bokadev.kompass.presentation.shared.formatDistance
import org.jetbrains.compose.resources.stringResource

/**
 * The expanded utility map: the map fills the screen; a compact sheet holds the selected point
 * and Navigate, with the nearby list one tap away; Recenter reframes all points. Selection is by
 * utility id everywhere (map, list, sheet and the Maps hand-off).
 */
@Composable
fun UtilityMapScreenContent(
    modifier: Modifier = Modifier,
    state: UtilityMapState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onUtilitySelect: (String) -> Unit,
    onNavigateClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val title = stringResource(state.category?.cardTitleRes() ?: Res.string.essentials_tile_all_on_map)
    val selectedItem = state.items.firstOrNull { it.utility.id == state.selectedUtilityId }
    var recenter by rememberSaveable { mutableIntStateOf(0) }
    var listOpen by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var sheetHeight by remember { mutableStateOf(0.dp) }

    Box(modifier.fillMaxSize().background(colors.nightBlack)) {
        if (state.items.isNotEmpty()) {
            NativeUtilityDetailMapView(
                pins = state.items.map { UtilityMapPin(id = it.utility.id, title = it.utility.name, point = it.point) },
                currentLocation = state.currentLocation,
                currentLocationLabel = stringResource(Res.string.essentials_you_are_here),
                focusedPinId = selectedItem?.utility?.id,
                onPinSelected = onUtilitySelect,
                recenterRequest = recenter,
                obscuredTop = topBarHeight,
                obscuredBottom = sheetHeight,
                modifier = Modifier.fillMaxSize()
            )
        }

        TopBar(title, stringResource(Res.string.detail_back), onBackClick, Modifier.onSizeChanged { topBarHeight = with(density) { it.height.toDp() } })

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { sheetHeight = with(density) { it.height.toDp() } },
            horizontalAlignment = Alignment.End
        ) {
            if (state.items.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.essentials_recenter),
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.nightBlack,
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 10.dp)
                        .heightIn(min = 48.dp)
                        .clip(KompassShapes.pill)
                        .background(colors.chalkWhite)
                        .clickable(role = Role.Button) { recenter++ }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }
            Sheet(state, selectedItem, listOpen, { listOpen = !listOpen }, onRetryClick, onUtilitySelect, onNavigateClick)
        }
    }
}

@Composable
private fun TopBar(title: String, backLabel: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KompassTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.nightBlack.copy(alpha = 0.85f))
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = backLabel },
            contentAlignment = Alignment.Center
        ) { ChevronLeftIcon(color = colors.chalkWhite, size = 18.dp) }
        Text(
            text = title,
            style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
            color = colors.boneWhite,
            modifier = Modifier
                .clip(KompassShapes.pill)
                .background(colors.nightBlack.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics { heading() }
        )
    }
}

@Composable
private fun Sheet(
    state: UtilityMapState,
    selectedItem: UtilityLocationItem?,
    listOpen: Boolean,
    onToggleList: () -> Unit,
    onRetryClick: () -> Unit,
    onUtilitySelect: (String) -> Unit,
    onNavigateClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val retryLabel = stringResource(Res.string.retry)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.charcoal)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val retry = onRetryClick
        when {
            state.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth(), color = colors.lampYellow, trackColor = colors.slateDark)
            state.error != null -> StateMessage(stringResource(state.error.messageRes), actionLabel = retryLabel, onAction = retry)
            state.items.isEmpty() -> StateMessage(stringResource(Res.string.essentials_no_utilities_found))
            else -> {
                if (state.refreshFailed) StateMessage(stringResource(Res.string.essentials_couldnt_load_utilities), actionLabel = retryLabel, onAction = retry)
                if (state.selectionGone) StateMessage(stringResource(Res.string.essentials_selection_gone))
                selectedItem?.let { item ->
                    Text(text = item.utility.name, style = KompassTheme.typography.gloockRegular23, color = colors.boneWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val meta = listOfNotNull(item.distanceMeters?.formatDistance(), item.utility.address?.takeIf { it.isNotBlank() }).joinToString(" · ")
                    if (meta.isNotBlank()) Text(text = meta, style = KompassTheme.typography.geologicaRegular14, color = colors.stoneGray)
                    val navigateToLabel = stringResource(Res.string.essentials_navigate_to, item.utility.name)
                    DetailPillButton(
                        label = stringResource(Res.string.essentials_navigate),
                        glyph = DetailGlyph.ArrowUpRight,
                        filled = true,
                        height = 52.dp,
                        onClick = { onNavigateClick(item.utility.id) },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = navigateToLabel }
                    )
                }
                Text(
                    text = if (listOpen) stringResource(Res.string.essentials_hide_list) else "${stringResource(Res.string.essentials_show_list)} (${state.items.size})",
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.lampYellow,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(KompassShapes.pill)
                        .clickable(role = Role.Button, onClick = onToggleList)
                        .padding(vertical = 14.dp)
                )
                if (listOpen) {
                    LazyColumn(Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(state.items, key = { it.utility.id }) { item ->
                            val selected = item.utility.id == selectedItem?.utility?.id
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) colors.lampShadow else colors.slateDark)
                                    .selectable(selected = selected, role = Role.RadioButton) { onUtilitySelect(item.utility.id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(text = item.utility.name, style = KompassTheme.typography.geologicaMedium16, color = colors.boneWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                val meta = listOfNotNull(item.distanceMeters?.formatDistance(), item.utility.address?.takeIf { it.isNotBlank() }).joinToString(" · ")
                                if (meta.isNotBlank()) Text(text = meta, style = KompassTheme.typography.geologicaRegular13, color = colors.stoneGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun UtilityMapScreenContentPreview() {
    val atm = Utility(id = "atm", name = "Euronet", category = UtilityCategory.ATM, latitude = 42.4250, longitude = 18.7710, address = "Old Town", googlePlaceId = null)
    KompassTheme {
        UtilityMapScreenContent(
            state = UtilityMapState(
                isLoading = false,
                category = UtilityCategory.ATM,
                items = listOf(UtilityLocationItem(atm, GeoPoint(42.4250, 18.7710), 86)),
                selectedUtilityId = "atm"
            ),
            onBackClick = {},
            onRetryClick = {},
            onUtilitySelect = {},
            onNavigateClick = {}
        )
    }
}
