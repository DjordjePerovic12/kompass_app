package llc.bokadev.kompass.core.util

/** Home wording added in the pilot refinement. "{area}" and "{name}" are replaced at the call site. */
data class HomeStrings(
    /** Headline when the device location is in use (the picker option stays "Device location"). */
    val nearYou: String,
    val changeAreaDescription: String,
    /** Editorial entry to a destination overview, e.g. "About Perast". */
    val aboutDestination: String
)

internal fun homeStringsFor(language: String): HomeStrings = when (language) {
    "fr" -> HomeStrings("Près de vous", "Changer de zone, actuellement {area}", "À propos de {name}")
    "de" -> HomeStrings("In deiner Nähe", "Gebiet ändern, derzeit {area}", "Über {name}")
    "es" -> HomeStrings("Cerca de ti", "Cambiar zona, ahora {area}", "Sobre {name}")
    "tr" -> HomeStrings("Yakınınızda", "Bölgeyi değiştir, şu an {area}", "{name} hakkında")
    else -> HomeStrings("Near you", "Change area, currently {area}", "About {name}")
}
