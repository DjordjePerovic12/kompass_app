package llc.bokadev.kompass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.utils.AppEnvironment
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

    val language by prefs.selectedLanguageFlow.collectAsState()

    AppEnvironment(languageTag = language) {
        KompassTheme {
            KompassAppNavigation(isFirstLaunch = prefs.isFirstLaunch())
        }
    }
}
