package llc.bokadev.kompass.core.util

/** Strings for Legends & Localisms (list + story detail). "{n}", "{d}", "{type}", "{title}" are replaced at the call site. */
data class StoriesStrings(
    val title: String,
    val subtitle: String,
    val all: String,
    val legends: String,
    val localisms: String,
    val legend: String,
    val localism: String,
    val minRead: String,
    val listen: String,
    val listenDuration: String,
    val emptyLegends: String,
    val emptyLocalisms: String,
    val emptyAll: String,
    val loadError: String,
    val storyLoadError: String,
    val listenToStory: String,
    val listenToStoryDuration: String,
    val pauseStory: String,
    val loadingStory: String,
    val whereItHappened: String,
    val map: String,
    val nextStory: String,
    val storyRegion: String,
    val audioAvailable: String,
    val minuteRead: String
)

internal fun storiesStringsFor(language: String): StoriesStrings = when (language) {
    "fr" -> StoriesStrings(
        title = "Légendes & expressions locales", subtitle = "Histoires et secrets des habitants", all = "Tout", legends = "Légendes",
        localisms = "Expressions", legend = "Légende", localism = "Expression locale", minRead = "{n} min de lecture", listen = "Écouter",
        listenDuration = "Écouter {d}", emptyLegends = "Pas encore de légendes.", emptyLocalisms = "Pas encore d'expressions locales.",
        emptyAll = "Pas encore d'histoires.", loadError = "Impossible de charger les histoires.", storyLoadError = "Impossible de charger cette histoire.",
        listenToStory = "Écouter cette histoire", listenToStoryDuration = "Écouter cette histoire · {d}", pauseStory = "Mettre l'histoire en pause",
        loadingStory = "Chargement de l'histoire, touchez pour annuler", whereItHappened = "Où cela s'est passé", map = "Carte",
        nextStory = "Histoire suivante", storyRegion = "Histoire", audioAvailable = "audio disponible", minuteRead = "{n} minutes de lecture"
    )
    "de" -> StoriesStrings(
        title = "Legenden & Lokales", subtitle = "Geschichten und Geheimnisse der Einheimischen", all = "Alle", legends = "Legenden",
        localisms = "Lokales", legend = "Legende", localism = "Lokaler Ausdruck", minRead = "{n} Min. Lesezeit", listen = "Anhören",
        listenDuration = "Anhören {d}", emptyLegends = "Noch keine Legenden.", emptyLocalisms = "Noch keine lokalen Ausdrücke.",
        emptyAll = "Noch keine Geschichten.", loadError = "Geschichten konnten nicht geladen werden.", storyLoadError = "Diese Geschichte konnte nicht geladen werden.",
        listenToStory = "Diese Geschichte anhören", listenToStoryDuration = "Diese Geschichte anhören · {d}", pauseStory = "Geschichte pausieren",
        loadingStory = "Geschichte lädt, tippen zum Abbrechen", whereItHappened = "Wo es geschah", map = "Karte",
        nextStory = "Nächste Geschichte", storyRegion = "Geschichte", audioAvailable = "Audio verfügbar", minuteRead = "{n} Minuten Lesezeit"
    )
    "es" -> StoriesStrings(
        title = "Leyendas y localismos", subtitle = "Historias y secretos de los locales", all = "Todo", legends = "Leyendas",
        localisms = "Localismos", legend = "Leyenda", localism = "Localismo", minRead = "{n} min de lectura", listen = "Escuchar",
        listenDuration = "Escuchar {d}", emptyLegends = "Aún no hay leyendas.", emptyLocalisms = "Aún no hay localismos.",
        emptyAll = "Aún no hay historias.", loadError = "No se pudieron cargar las historias.", storyLoadError = "No se pudo cargar esta historia.",
        listenToStory = "Escuchar esta historia", listenToStoryDuration = "Escuchar esta historia · {d}", pauseStory = "Pausar la historia",
        loadingStory = "Cargando la historia, toca para cancelar", whereItHappened = "Dónde ocurrió", map = "Mapa",
        nextStory = "Siguiente historia", storyRegion = "Historia", audioAvailable = "audio disponible", minuteRead = "{n} minutos de lectura"
    )
    "tr" -> StoriesStrings(
        title = "Efsaneler ve Yerel Deyişler", subtitle = "Yerlilerden hikâyeler ve sırlar", all = "Tümü", legends = "Efsaneler",
        localisms = "Deyişler", legend = "Efsane", localism = "Yerel deyiş", minRead = "{n} dk okuma", listen = "Dinle",
        listenDuration = "Dinle {d}", emptyLegends = "Henüz efsane yok.", emptyLocalisms = "Henüz yerel deyiş yok.",
        emptyAll = "Henüz hikâye yok.", loadError = "Hikâyeler yüklenemedi.", storyLoadError = "Bu hikâye yüklenemedi.",
        listenToStory = "Bu hikâyeyi dinle", listenToStoryDuration = "Bu hikâyeyi dinle · {d}", pauseStory = "Hikâyeyi duraklat",
        loadingStory = "Hikâye yükleniyor, iptal için dokunun", whereItHappened = "Nerede geçti", map = "Harita",
        nextStory = "Sonraki hikâye", storyRegion = "Hikâye", audioAvailable = "ses mevcut", minuteRead = "{n} dakika okuma"
    )
    else -> StoriesStrings(
        title = "Legends & Localisms", subtitle = "Stories and secrets from the locals", all = "All", legends = "Legends",
        localisms = "Localisms", legend = "Legend", localism = "Localism", minRead = "{n} min read", listen = "Listen",
        listenDuration = "Listen {d}", emptyLegends = "No legends yet.", emptyLocalisms = "No localisms yet.",
        emptyAll = "No stories yet.", loadError = "Couldn't load the stories.", storyLoadError = "Couldn't load this story.",
        listenToStory = "Listen to this story", listenToStoryDuration = "Listen to this story · {d}", pauseStory = "Pause story",
        loadingStory = "Loading the story, tap to cancel", whereItHappened = "Where it happened", map = "Map",
        nextStory = "Next story", storyRegion = "Story", audioAvailable = "audio available", minuteRead = "{n} minute read"
    )
}
