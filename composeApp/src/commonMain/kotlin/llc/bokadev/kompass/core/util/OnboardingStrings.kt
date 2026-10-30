package llc.bokadev.kompass.core.util

/** The short first-run introduction. Promises only what the pilot actually offers. */
data class OnboardingStrings(
    val title: String,
    val body: String,
    val pointLocals: String,
    val pointPractical: String,
    val pointStories: String,
    val start: String
)

internal fun onboardingStringsFor(language: String): OnboardingStrings = when (language) {
    "fr" -> OnboardingStrings(
        "Kotor, raconté par ceux qui y vivent.",
        "Un guide de poche pour la baie, sans classements payants.",
        "Lieux et adresses choisis par des habitants",
        "Infos pratiques : transports, argent, urgences",
        "Histoires et audio pour une sélection de lieux",
        "Commencer"
    )
    "de" -> OnboardingStrings(
        "Kotor, erzählt von Menschen, die hier leben.",
        "Ein Taschenführer für die Bucht, ohne bezahlte Platzierungen.",
        "Orte und Restaurants, ausgewählt von Einheimischen",
        "Praktisches: unterwegs sein, Geld, Notfälle",
        "Geschichten und Audio zu ausgewählten Orten",
        "Los geht's"
    )
    "es" -> OnboardingStrings(
        "Kotor, contado por quienes viven aquí.",
        "Una guía de bolsillo para la bahía, sin clasificaciones pagadas.",
        "Lugares y comida elegidos por gente local",
        "Información práctica: transporte, dinero, emergencias",
        "Historias y audio para lugares seleccionados",
        "Empezar"
    )
    "tr" -> OnboardingStrings(
        "Kotor, burada yaşayanların anlatımıyla.",
        "Körfez için ücretli sıralaması olmayan bir cep rehberi.",
        "Yerel halkın seçtiği yerler ve lezzetler",
        "Pratik bilgiler: ulaşım, para, acil durumlar",
        "Seçili yerler için hikâyeler ve sesli anlatım",
        "Başla"
    )
    else -> OnboardingStrings(
        "Kotor, from the people who live here.",
        "A pocket guide to the bay, with no paid rankings.",
        "Places and food chosen by locals",
        "Practical help: getting around, money, emergencies",
        "Stories and audio for selected places",
        "Start exploring"
    )
}
