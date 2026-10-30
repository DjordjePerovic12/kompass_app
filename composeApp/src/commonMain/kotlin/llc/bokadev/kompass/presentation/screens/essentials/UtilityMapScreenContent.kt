package llc.bokadev.kompass.presentation.screens.essentials

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
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.mvi.asString
import llc.bokadev.kompass.core.utils.EssentialsStrings
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.detail.DetailPillButton
import llc.bokadev.kompass.presentation.shared.NativeUtilityDetailMapView
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.formatDistance
import llc.bokadev.kompass.presentation.shared.title

/**
 * The expanded utility map: the map fills the screen; a compact sheet holds the selected point
 * and Navigate, with the nearby list one tap away; Recenter reframes all points. Selection is by
 * utility id everywhere (map, list, sheet and the Maps hand-off).
 */
@Composable
fun UtilityMapScreenContent(
    state: UtilityMapState,
    onAction: (UtilityMapAction) -> Unit
) {
    val colors = KompassTheme.colors
    val strings = rememberAppStrings()
    val s = strings.essentials
    val title = state.category?.title(s) ?: s.tileAllOnMap
    val selectedItem = state.items.firstOrNull { it.utility.id == state.selectedUtilityId }
    var recenter by rememberSaveable { mutableIntStateOf(0) }
    var listOpen by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var sheetHeight by remember { mutableStateOf(0.dp) }

    Box(Modifier.fillMaxSize().background(colors.nightBlack)) {
        if (state.items.isNotEmpty()) {
            NativeUtilityDetailMapView(
                pins = state.items.map { UtilityMapPin(id = it.utility.id, title = it.utility.name, point = it.point) },
                currentLocation = state.currentLocation,
                currentLocationLabel = s.youAreHere,
                focusedPinId = selectedItem?.utility?.id,
                onPinSelected = { id -> onAction(UtilityMapAction.SelectUtility(id)) },
                recenterRequest = recenter,
                obscuredTop = topBarHeight,
                obscuredBottom = sheetHeight,
                modifier = Modifier.fillMaxSize()
            )
        }

        TopBar(title, strings.detail.back, { onAction(UtilityMapAction.Back) }, Modifier.onSizeChanged { topBarHeight = with(density) { it.height.toDp() } })

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { sheetHeight = with(density) { it.height.toDp() } },
            horizontalAlignment = Alignment.End
        ) {
            if (state.items.isNotEmpty()) {
                Text(
                    text = s.recenter,
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
            Sheet(state, s, strings.retry, selectedItem, listOpen, { listOpen = !listOpen }, onAction)
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
    s: EssentialsStrings,
    retryLabel: String,
    selectedItem: UtilityLocationItem?,
    listOpen: Boolean,
    onToggleList: () -> Unit,
    onAction: (UtilityMapAction) -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.charcoal)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val retry = { onAction(UtilityMapAction.Retry) }
        when {
            state.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth(), color = colors.lampYellow, trackColor = colors.slateDark)
            state.error != null -> StateMessage(state.error.asString(), actionLabel = retryLabel, onAction = retry)
            state.items.isEmpty() -> StateMessage(s.noUtilitiesFound)
            else -> {
                if (state.refreshFailed) StateMessage(s.couldntLoadUtilities, actionLabel = retryLabel, onAction = retry)
                if (state.selectionGone) StateMessage(s.selectionGone)
                selectedItem?.let { item ->
                    Text(text = item.utility.name, style = KompassTheme.typography.gloockRegular23, color = colors.boneWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val meta = listOfNotNull(item.distanceMeters?.formatDistance(), item.utility.address?.takeIf { it.isNotBlank() }).joinToString(" · ")
                    if (meta.isNotBlank()) Text(text = meta, style = KompassTheme.typography.geologicaRegular14, color = colors.stoneGray)
                    DetailPillButton(
                        label = s.navigate,
                        glyph = DetailGlyph.ArrowUpRight,
                        filled = true,
                        height = 52.dp,
                        onClick = { onAction(UtilityMapAction.Navigate(item.utility.id)) },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = s.navigateTo.replace("{name}", item.utility.name) }
                    )
                }
                Text(
                    text = if (listOpen) s.hideList else "${s.showList} (${state.items.size})",
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
                                    .selectable(selected = selected, role = Role.RadioButton) { onAction(UtilityMapAction.SelectUtility(item.utility.id)) }
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

internal fun UtilityCategory.title(s: EssentialsStrings): String = when (this) {
    UtilityCategory.ATM -> s.titleAtm
    UtilityCategory.PHARMACY -> s.titlePharmacy
    UtilityCategory.SUPERMARKET -> s.titleSupermarket
    UtilityCategory.SHOP -> s.titleShop
    UtilityCategory.PARKING -> s.titleParking
    UtilityCategory.GAS_STATION -> s.titleFuel
    UtilityCategory.EMERGENCY -> s.titleEmergency
}
