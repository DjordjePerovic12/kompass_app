package llc.bokadev.kompass.presentation.shared

import llc.bokadev.kompass.core.presentation.SnackbarBus
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_couldnt_open_browser
import kompass.composeapp.generated.resources.detail_couldnt_open_dialer
import kompass.composeapp.generated.resources.detail_couldnt_open_maps
import org.jetbrains.compose.resources.getString

/** A hand-off to another app, requested by a ViewModel and performed by the screen's orchestrator. */
sealed interface ExternalLaunch {
    data class Maps(val latitude: Double, val longitude: Double, val label: String) : ExternalLaunch
    data class Url(val url: String) : ExternalLaunch
    data class Dial(val phone: String) : ExternalLaunch
    /** This app's page in system settings (e.g. to turn location on). */
    data object AppSettings : ExternalLaunch
}

/** Performs [launch]; if nothing on the device can handle it, says so instead of failing silently. */
suspend fun ExternalLauncher.perform(launch: ExternalLaunch, snackbar: SnackbarBus) {
    val opened = when (launch) {
        is ExternalLaunch.Maps -> openMaps(launch.latitude, launch.longitude, launch.label)
        is ExternalLaunch.Url -> openUrl(launch.url)
        is ExternalLaunch.Dial -> dial(launch.phone)
        ExternalLaunch.AppSettings -> openAppSettings()
    }
    if (!opened) snackbar.show(
        when (launch) {
            is ExternalLaunch.Maps -> getString(Res.string.detail_couldnt_open_maps)
            is ExternalLaunch.Url -> getString(Res.string.detail_couldnt_open_browser)
            is ExternalLaunch.Dial -> getString(Res.string.detail_couldnt_open_dialer)
            ExternalLaunch.AppSettings -> return
        }
    )
}
