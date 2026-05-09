package llc.bokadev.kompass.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject

data class LanguageOption(
    val code: String,
    val label: String
)

val supportedLanguages = listOf(
    LanguageOption("en", "English"),
    LanguageOption("fr", "Français"),
    LanguageOption("tr", "Türkçe"),
    LanguageOption("es", "Español"),
    LanguageOption("de", "Deutsch")
)

@Composable
fun currentAppLanguage(): String {
    val preferences = koinInject<AppPreferences>()
    val selectedLanguage by preferences.selectedLanguageFlow.collectAsState()
    return selectedLanguage
}
