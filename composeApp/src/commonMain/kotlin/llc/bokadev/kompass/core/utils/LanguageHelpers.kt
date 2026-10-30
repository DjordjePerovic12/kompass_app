package llc.bokadev.kompass.core.utils

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

/** The app's languages, each with its own name (endonym), in display order. */
val SUPPORTED_LANGUAGES: List<Pair<String, String>> = listOf(
    "en" to "English",
    "de" to "Deutsch",
    "fr" to "Français",
    "es" to "Español",
    "tr" to "Türkçe"
)

/** The phone's language if the app supports it, else English. */
fun supportedDeviceLanguage(): String =
    deviceLanguageCode().lowercase().takeIf { code -> SUPPORTED_LANGUAGES.any { it.first == code } } ?: "en"

/** The device's preferred language as an ISO 639-1 code (e.g. "de"). */
expect fun deviceLanguageCode(): String
