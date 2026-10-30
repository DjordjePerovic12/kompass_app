package llc.bokadev.kompass.core.util

/** Strings added for the Lamplight Explore screen. "{date}" is replaced at the call site. */
data class ExploreStrings(
    val furtherOut: String,
    val read: String,
    val readListen: String,
    val storiesSubtitle: String,
    val whatsOn: String,
    val nextEvent: String,
    val importantNoticeActive: String
)

internal fun exploreStringsFor(language: String): ExploreStrings = when (language) {
    "fr" -> ExploreStrings(
        furtherOut = "Plus loin",
        read = "Lire",
        readListen = "Lire · Écouter",
        storiesSubtitle = "Histoires et traditions locales, racontées comme on les raconte ici.",
        whatsOn = "Au programme & actualités",
        nextEvent = "prochain événement {date}",
        importantNoticeActive = "avis important en cours"
    )
    "de" -> ExploreStrings(
        furtherOut = "Weiter draußen",
        read = "Lesen",
        readListen = "Lesen · Hören",
        storiesSubtitle = "Lokale Geschichten und Bräuche, so erzählt, wie man sie hier erzählt.",
        whatsOn = "Was los ist & Neuigkeiten",
        nextEvent = "nächste Veranstaltung {date}",
        importantNoticeActive = "wichtiger Hinweis aktiv"
    )
    "es" -> ExploreStrings(
        furtherOut = "Más lejos",
        read = "Leer",
        readListen = "Leer · Escuchar",
        storiesSubtitle = "Historias y tradiciones locales, contadas como se cuentan aquí.",
        whatsOn = "Agenda y novedades",
        nextEvent = "próximo evento {date}",
        importantNoticeActive = "aviso importante activo"
    )
    "tr" -> ExploreStrings(
        furtherOut = "Biraz uzakta",
        read = "Oku",
        readListen = "Oku · Dinle",
        storiesSubtitle = "Yerel hikâyeler ve gelenekler, burada anlatıldığı gibi.",
        whatsOn = "Etkinlikler ve duyurular",
        nextEvent = "sıradaki etkinlik {date}",
        importantNoticeActive = "önemli duyuru var"
    )
    else -> ExploreStrings(
        furtherOut = "Further out",
        read = "Read",
        readListen = "Read · Listen",
        storiesSubtitle = "Local stories and traditions, told the way people here tell them.",
        whatsOn = "What's on & updates",
        nextEvent = "next event {date}",
        importantNoticeActive = "important notice active"
    )
}
