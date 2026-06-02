package llc.bokadev.kompass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.repository.DeepPurchaseRepository
import llc.bokadev.kompass.presentation.navigation.KompassNavHost
import llc.bokadev.kompass.presentation.theme.KOmpassTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val prefs = koinInject<AppPreferences>()
    val deepPurchaseRepository = koinInject<DeepPurchaseRepository>()

    LaunchedEffect(Unit) {
        deepPurchaseRepository.syncEntitlements()
    }

    KOmpassTheme {
        KompassNavHost(isFirstLaunch = prefs.isFirstLaunch())
    }
}
