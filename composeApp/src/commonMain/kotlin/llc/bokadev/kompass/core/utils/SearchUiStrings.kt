package llc.bokadev.kompass.core.utils

/** Strings for the Lamplight Search screen. "{n}", "{q}", "{type}" are replaced at the call site. */
data class SearchUiStrings(
    val placeholder: String,
    val clearSearch: String,
    val recent: String,
    val clear: String,
    val tryLabel: String,
    /** Editorial "Try" suggestions. Never generated, never trending. */
    val suggestions: List<String>,
    val all: String,
    val places: String,
    val activities: String,
    val stories: String,
    val essentials: String,
    val events: String,
    val place: String,
    val activity: String,
    val story: String,
    val essential: String,
    val event: String,
    val chipCount: String,
    val resultsCount: String,
    val oneResult: String,
    val readOrListen: String,
    val showMore: String,
    val nothingFor: String,
    val nothingBody: String,
    val browseExplore: String,
    val notReady: String,
    val retry: String,
    val mapPoints: String,
    val services: String,
    val service: String,
    val updates: String,
    val update: String,
    /** Shown above results (or "nothing") when some content couldn't be loaded. */
    val incomplete: String,
    /** Context line of a utility category result: "{n} on the map". */
    val onMap: String
)

internal fun searchUiStringsFor(language: String): SearchUiStrings = when (language) {
    "fr" -> SearchUiStrings(
        placeholder = "Rechercher à Kotor et dans la baie", clearSearch = "Effacer la recherche", recent = "RÉCENT",
        clear = "Effacer", tryLabel = "ESSAYEZ", suggestions = listOf("Perast", "Cathédrale", "Gare routière", "Bateau", "Lovćen"),
        all = "Tout", places = "Lieux", activities = "Activités", stories = "Histoires", essentials = "Essentiels", events = "Événements",
        place = "Lieu", activity = "Activité", story = "Histoire", essential = "Essentiel", event = "Événement",
        chipCount = "{type}, {n} résultats", resultsCount = "{n} résultats à Kotor et dans la baie",
        oneResult = "1 résultat à Kotor et dans la baie", readOrListen = "Lire ou écouter", showMore = "Afficher plus",
        nothingFor = "Rien pour « {q} »",
        nothingBody = "KOMPASS cherche uniquement à Kotor et dans la baie. Vérifiez l'orthographe ou essayez un mot plus court.",
        browseExplore = "Parcourir Explorer", notReady = "La recherche n'est pas encore prête. Connectez-vous une fois pour télécharger les lieux.",
        retry = "Réessayer",
        mapPoints = "Points sur la carte", services = "Services", service = "Service", updates = "Infos locales", update = "Info locale",
        incomplete = "Une partie du contenu n'a pas pu être chargée : les résultats peuvent être incomplets.", onMap = "{n} sur la carte"
    )
    "de" -> SearchUiStrings(
        placeholder = "Kotor und die Bucht durchsuchen", clearSearch = "Suche löschen", recent = "ZULETZT",
        clear = "Löschen", tryLabel = "PROBIER", suggestions = listOf("Perast", "Kathedrale", "Busbahnhof", "Boot", "Lovćen"),
        all = "Alle", places = "Orte", activities = "Aktivitäten", stories = "Geschichten", essentials = "Wichtiges", events = "Events",
        place = "Ort", activity = "Aktivität", story = "Geschichte", essential = "Wichtiges", event = "Event",
        chipCount = "{type}, {n} Ergebnisse", resultsCount = "{n} Ergebnisse in Kotor und der Bucht",
        oneResult = "1 Ergebnis in Kotor und der Bucht", readOrListen = "Lesen oder anhören", showMore = "Mehr anzeigen",
        nothingFor = "Nichts für „{q}“",
        nothingBody = "KOMPASS sucht nur in Kotor und der Bucht. Prüfe die Schreibweise oder versuche ein kürzeres Wort.",
        browseExplore = "Entdecken durchstöbern", notReady = "Die Suche ist noch nicht bereit. Verbinde dich einmal, um Orte zu laden.",
        retry = "Erneut versuchen",
        mapPoints = "Kartenpunkte", services = "Services", service = "Service", updates = "Lokale Hinweise", update = "Lokaler Hinweis",
        incomplete = "Einige Inhalte konnten nicht geladen werden, daher fehlen eventuell Ergebnisse.", onMap = "{n} auf der Karte"
    )
    "es" -> SearchUiStrings(
        placeholder = "Buscar en Kotor y la bahía", clearSearch = "Borrar búsqueda", recent = "RECIENTES",
        clear = "Borrar", tryLabel = "PRUEBA", suggestions = listOf("Perast", "Catedral", "Estación de autobuses", "Barco", "Lovćen"),
        all = "Todo", places = "Lugares", activities = "Actividades", stories = "Historias", essentials = "Esenciales", events = "Eventos",
        place = "Lugar", activity = "Actividad", story = "Historia", essential = "Esencial", event = "Evento",
        chipCount = "{type}, {n} resultados", resultsCount = "{n} resultados en Kotor y la bahía",
        oneResult = "1 resultado en Kotor y la bahía", readOrListen = "Leer o escuchar", showMore = "Mostrar más",
        nothingFor = "Nada para «{q}»",
        nothingBody = "KOMPASS solo busca en Kotor y la bahía. Revisa la ortografía o prueba una palabra más corta.",
        browseExplore = "Ver Explorar", notReady = "La búsqueda aún no está lista. Conéctate una vez para descargar los lugares.",
        retry = "Reintentar",
        mapPoints = "Puntos del mapa", services = "Servicios", service = "Servicio", updates = "Avisos locales", update = "Aviso local",
        incomplete = "Parte del contenido no se pudo cargar, así que pueden faltar resultados.", onMap = "{n} en el mapa"
    )
    "tr" -> SearchUiStrings(
        placeholder = "Kotor ve körfezde ara", clearSearch = "Aramayı temizle", recent = "SON ARAMALAR",
        clear = "Temizle", tryLabel = "DENEYİN", suggestions = listOf("Perast", "Katedral", "Otogar", "Tekne", "Lovćen"),
        all = "Tümü", places = "Yerler", activities = "Aktiviteler", stories = "Hikâyeler", essentials = "Temel bilgiler", events = "Etkinlikler",
        place = "Yer", activity = "Aktivite", story = "Hikâye", essential = "Temel bilgi", event = "Etkinlik",
        chipCount = "{type}, {n} sonuç", resultsCount = "Kotor ve körfezde {n} sonuç",
        oneResult = "Kotor ve körfezde 1 sonuç", readOrListen = "Oku veya dinle", showMore = "Daha fazla göster",
        nothingFor = "\"{q}\" için sonuç yok",
        nothingBody = "KOMPASS yalnızca Kotor ve körfezde arar. Yazımı kontrol edin veya daha kısa bir kelime deneyin.",
        browseExplore = "Keşfet'e göz at", notReady = "Arama henüz hazır değil. Yerleri indirmek için bir kez bağlanın.",
        retry = "Tekrar dene",
        mapPoints = "Harita noktaları", services = "Hizmetler", service = "Hizmet", updates = "Yerel duyurular", update = "Yerel duyuru",
        incomplete = "Bazı içerikler yüklenemedi, sonuçlar eksik olabilir.", onMap = "Haritada {n} nokta"
    )
    else -> SearchUiStrings(
        placeholder = "Search Kotor and the bay", clearSearch = "Clear search", recent = "RECENT",
        clear = "Clear", tryLabel = "TRY", suggestions = listOf("Perast", "Cathedral", "Bus station", "Boat", "Lovćen"),
        all = "All", places = "Places", activities = "Activities", stories = "Stories", essentials = "Essentials", events = "Events",
        place = "Place", activity = "Activity", story = "Story", essential = "Essentials", event = "Event",
        chipCount = "{type}, {n} results", resultsCount = "{n} results in Kotor & the bay",
        oneResult = "1 result in Kotor & the bay", readOrListen = "Read or listen", showMore = "Show more",
        nothingFor = "Nothing for “{q}”",
        nothingBody = "KOMPASS only searches Kotor and the bay. Check the spelling, or try a shorter word.",
        browseExplore = "Browse Explore", notReady = "Search isn't ready yet. Connect once to download places.",
        retry = "Retry",
        mapPoints = "Map points", services = "Services", service = "Service", updates = "Local updates", update = "Local update",
        incomplete = "Some content couldn't load, so results may be incomplete.", onMap = "{n} on the map"
    )
}
