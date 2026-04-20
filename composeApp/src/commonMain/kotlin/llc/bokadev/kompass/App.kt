package llc.bokadev.kompass

import androidx.compose.runtime.Composable
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.presentation.navigation.KompassNavHost
import llc.bokadev.kompass.presentation.theme.KOmpassTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val prefs = koinInject<AppPreferences>()
    KOmpassTheme {
        KompassNavHost(isFirstLaunch = prefs.isFirstLaunch())
    }
}
