package llc.bokadev.kompass.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.home_change_area_description
import kompass.composeapp.generated.resources.home_near_you
import kompass.composeapp.generated.resources.redesign_around_label
import kompass.composeapp.generated.resources.redesign_chosen_area_status
import kompass.composeapp.generated.resources.redesign_fallback
import kompass.composeapp.generated.resources.redesign_use_my_location_link
import kompass.composeapp.generated.resources.search_placeholder
import llc.bokadev.kompass.domain.model.HomeArea
import llc.bokadev.kompass.presentation.home.HomeSection
import llc.bokadev.kompass.presentation.home.HomeState
import llc.bokadev.kompass.presentation.home.SectionStatus
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassDimens
import llc.bokadev.kompass.presentation.shared.icons.ChevronDownIcon
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import llc.bokadev.kompass.presentation.shared.shortLabel

/**
 * "Lamplight" top bar (spec §3.1): the shared [LamplightHeader] with Home's "Around {area}" title
 * (opens the area picker) and a status line explaining where results are coming from. Lives directly in `components/` (not folded into `HomeMasthead`/`HomeAreaControl`) since
 * the spec treats it as one cohesive unit, replacing both.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LamplightTopBar(
    state: HomeState,
    onAreaClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onSearchClick: () -> Unit,
    onUseMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    // With the device location in use the headline is visitor-facing ("Near you"); "Device
    // location" stays only as the option name in the area picker.
    val usingDevice = state.area == HomeArea.DEVICE && state.isUsingCurrentLocation
    val areaBigLabel = if (usingDevice) stringResource(Res.string.home_near_you) else state.effectiveArea.shortLabel()

    LamplightHeader(
        searchPlaceholder = stringResource(Res.string.search_placeholder),
        onLanguageClick = onLanguageClick,
        onSearchClick = onSearchClick,
        modifier = modifier,
        footer = {
            val statusText = homeStatusText(
                state = state,
                chosenAreaLabel = stringResource(Res.string.redesign_chosen_area_status),
                fallbackLabel = stringResource(Res.string.redesign_fallback)
            )
            if (statusText != null) {
                // Flows: when the status is long (or translated), the link moves whole to the next
                // line instead of being squeezed into a one-word-wide column.
                FlowRow(itemVerticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = statusText,
                        style = KompassTheme.typography.geologicaRegular13.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = colors.nightBlack.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "· ${stringResource(Res.string.redesign_use_my_location_link)}",
                        style = KompassTheme.typography.geologicaRegular13.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        ),
                        color = colors.nightBlack,
                        modifier = Modifier
                            .heightIn(min = KompassDimens.minTouchTarget)
                            .clickable(onClick = onUseMyLocationClick)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }
        }
    ) {
        val changeAreaDescription = stringResource(Res.string.home_change_area_description, areaBigLabel)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = KompassDimens.minTouchTarget)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onAreaClick)
                .semantics { contentDescription = changeAreaDescription },
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!usingDevice) {
                Text(
                    text = stringResource(Res.string.redesign_around_label),
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    color = colors.nightBlack
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = areaBigLabel,
                    style = KompassTheme.typography.gloockRegular40,
                    color = colors.nightBlack,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                ChevronDownIcon(color = colors.nightBlack, size = 22.dp)
            }
        }
    }
}

/**
 * Null when location is actively in use and nothing needs explaining. Never re-prompts for
 * permission on its own — the link it sits next to only acts on an explicit tap.
 */
private fun homeStatusText(state: HomeState, chosenAreaLabel: String, fallbackLabel: String): String? = when {
    state.area != HomeArea.DEVICE -> chosenAreaLabel
    !state.isUsingCurrentLocation && state.sections[HomeSection.NEARBY] != SectionStatus.LOADING ->
        fallbackLabel
    else -> null
}

@Preview
@Composable
private fun LamplightTopBarPreview() {
    KompassTheme {
        LamplightTopBar(
            state = HomeState(area = HomeArea.PERAST),
            onAreaClick = {},
            onLanguageClick = {},
            onSearchClick = {},
            onUseMyLocationClick = {},
            modifier = Modifier.pageGradientBackground()
        )
    }
}
