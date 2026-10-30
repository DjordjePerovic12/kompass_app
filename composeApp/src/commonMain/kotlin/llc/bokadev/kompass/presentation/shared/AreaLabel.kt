package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.nearby_you
import kompass.composeapp.generated.resources.redesign_around_dobrota
import kompass.composeapp.generated.resources.redesign_around_old_town
import kompass.composeapp.generated.resources.redesign_around_perast
import kompass.composeapp.generated.resources.redesign_device_location
import kompass.composeapp.generated.resources.redesign_old_town
import kompass.composeapp.generated.resources.redesign_old_town_short
import kompass.composeapp.generated.resources.saved_group_other
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.domain.model.HomeArea
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The short visitor-facing name of an area key ("old_town" → "Old Town"); unknown keys are title-cased. */
@Composable
fun String.areaLabel(): String = when (this) {
    GROUP_OTHER -> stringResource(Res.string.saved_group_other)
    else -> HomeArea.entries.firstOrNull { it.preferenceValue == this }?.shortLabel() ?: toTitleCaseLabel()
}

/** The area as offered in the area picker (e.g. "Kotor / Old Town"). */
@Composable
fun HomeArea.label(): String = when (this) {
    HomeArea.DEVICE -> stringResource(Res.string.redesign_device_location)
    HomeArea.OLD_TOWN -> stringResource(Res.string.redesign_old_town)
    HomeArea.DOBROTA -> "Dobrota"
    HomeArea.PERAST -> "Perast"
}

/** Just the area's own name (e.g. "Old Town", not "Kotor / Old Town") — for the Lamplight
 * top bar's large title, which already has its own "Around" label above it. */
@Composable
fun HomeArea.shortLabel(): String = when (this) {
    HomeArea.DEVICE -> stringResource(Res.string.redesign_device_location)
    HomeArea.OLD_TOWN -> stringResource(Res.string.redesign_old_town_short)
    HomeArea.DOBROTA -> "Dobrota"
    HomeArea.PERAST -> "Perast"
}

/** Home's Nearby section heading for the area ("Nearby You", "Around Perast"). */
fun HomeArea.sectionTitleRes(): StringResource = when (this) {
    HomeArea.DEVICE -> Res.string.nearby_you
    HomeArea.OLD_TOWN -> Res.string.redesign_around_old_town
    HomeArea.DOBROTA -> Res.string.redesign_around_dobrota
    HomeArea.PERAST -> Res.string.redesign_around_perast
}

@Composable
fun HomeArea.sectionTitle(): String = stringResource(sectionTitleRes())
