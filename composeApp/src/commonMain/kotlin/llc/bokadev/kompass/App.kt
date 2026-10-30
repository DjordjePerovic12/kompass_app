package llc.bokadev.kompass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.PilotFeatures
import llc.bokadev.kompass.domain.repository.DeepPurchaseRepository
import llc.bokadev.kompass.core.navigation.KompassAppNavigation
import org.koin.compose.koinInject
import llc.bokadev.kompass.ui.theme.KompassTheme

@Composable
fun App() {
    val prefs = koinInject<AppPreferences>()
    if (PilotFeatures.DEEP_ENABLED) {
        val deepPurchaseRepository = koinInject<DeepPurchaseRepository>()
        LaunchedEffect(Unit) {
            deepPurchaseRepository.syncEntitlements()
        }
    }

    // AppEnvironment (core/utils/AppLocale.kt) gets wired here with the first screen that reads
    // Res.string: the template version re-creates the UI on a language change, which would reset
    // the selected tab; until then strings come from AppStrings and need no locale override.
    KompassTheme {
        KompassAppNavigation(isFirstLaunch = prefs.isFirstLaunch())
    }
}
