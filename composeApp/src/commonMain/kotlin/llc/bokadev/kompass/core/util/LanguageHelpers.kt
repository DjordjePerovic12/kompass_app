package llc.bokadev.kompass.core.util

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
fun currentAppLanguage(): String {
    val preferences = koinInject<AppPreferences>()
    return preferences.getSelectedLanguage()
}
