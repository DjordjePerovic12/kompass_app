package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_device_location
import kompass.composeapp.generated.resources.redesign_old_town_short
import kompass.composeapp.generated.resources.saved_group_other
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.presentation.screens.favorites.GROUP_OTHER
import llc.bokadev.kompass.presentation.screens.home.HomeArea
import org.jetbrains.compose.resources.stringResource

/** The short visitor-facing name of an area key ("old_town" → "Old Town"); unknown keys are title-cased. */
@Composable
fun String.areaLabel(): String = when (this) {
    GROUP_OTHER -> stringResource(Res.string.saved_group_other)
    else -> when (HomeArea.entries.firstOrNull { it.preferenceValue == this }) {
        HomeArea.DEVICE -> stringResource(Res.string.redesign_device_location)
        HomeArea.OLD_TOWN -> stringResource(Res.string.redesign_old_town_short)
        HomeArea.DOBROTA -> "Dobrota"
        HomeArea.PERAST -> "Perast"
        null -> toTitleCaseLabel()
    }
}
