package llc.bokadev.kompass.core.utils

/** Strings for the language bottom sheet. Language names are given in the current UI language. */
data class LanguageStrings(
    val title: String,
    val subtitle: String,
    val current: String,
    val usePhoneLanguage: String,
    val close: String,
    val names: Map<String, String>
)

/** The app's languages, each with its own name (endonym), in display order. */
val SUPPORTED_LANGUAGES: List<Pair<String, String>> = listOf(
    "en" to "English",
    "de" to "Deutsch",
    "fr" to "Français",
    "es" to "Español",
    "tr" to "Türkçe"
)

internal fun languageStringsFor(language: String): LanguageStrings = when (language) {
    "fr" -> LanguageStrings(
        title = "Langue",
        subtitle = "Change les lieux, les récits et l'audio. Ce qui n'est pas encore traduit reste en anglais.",
        current = "Actuelle",
        usePhoneLanguage = "Utiliser la langue du téléphone",
        close = "Fermer",
        names = mapOf("en" to "Anglais", "de" to "Allemand", "fr" to "Français", "es" to "Espagnol", "tr" to "Turc")
    )
    "de" -> LanguageStrings(
        title = "Sprache",
        subtitle = "Ändert Orte, Geschichten und Audio. Was noch nicht übersetzt ist, bleibt auf Englisch.",
        current = "Aktuell",
        usePhoneLanguage = "Sprache des Telefons verwenden",
        close = "Schließen",
        names = mapOf("en" to "Englisch", "de" to "Deutsch", "fr" to "Französisch", "es" to "Spanisch", "tr" to "Türkisch")
    )
    "es" -> LanguageStrings(
        title = "Idioma",
        subtitle = "Cambia los lugares, las historias y el audio. Lo que aún no está traducido sigue en inglés.",
        current = "Actual",
        usePhoneLanguage = "Usar el idioma del teléfono",
        close = "Cerrar",
        names = mapOf("en" to "Inglés", "de" to "Alemán", "fr" to "Francés", "es" to "Español", "tr" to "Turco")
    )
    "tr" -> LanguageStrings(
        title = "Dil",
        subtitle = "Yerleri, hikâyeleri ve sesleri değiştirir. Henüz çevrilmemiş içerik İngilizce kalır.",
        current = "Geçerli",
        usePhoneLanguage = "Telefonun dilini kullan",
        close = "Kapat",
        names = mapOf("en" to "İngilizce", "de" to "Almanca", "fr" to "Fransızca", "es" to "İspanyolca", "tr" to "Türkçe")
    )
    else -> LanguageStrings(
        title = "Language",
        subtitle = "Changes places, stories and audio. Anything not yet translated stays in English.",
        current = "Current",
        usePhoneLanguage = "Use my phone's language",
        close = "Close",
        names = mapOf("en" to "English", "de" to "German", "fr" to "French", "es" to "Spanish", "tr" to "Turkish")
    )
}

/** The phone's language if the app supports it, else English. */
fun supportedDeviceLanguage(): String =
    deviceLanguageCode().lowercase().takeIf { code -> SUPPORTED_LANGUAGES.any { it.first == code } } ?: "en"

/** The device's preferred language as an ISO 639-1 code (e.g. "de"). */
expect fun deviceLanguageCode(): String
