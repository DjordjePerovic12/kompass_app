package llc.bokadev.kompass.core.util

/**
 * Shared visitor-facing loading / empty / error / offline wording (pilot state contract) for the
 * Events, Local updates and Services screens. Never shows raw exceptions.
 */
data class StateStrings(
    val filterWhen: String,
    val filterType: String,
    val resetFilters: String,
    val couldntLoadEvents: String,
    val couldntLoadUpdates: String,
    val couldntLoadServices: String,
    val couldntRefresh: String,
    val noUpdates: String,
    val noServices: String,
    val noticeWhen: String,
    val noticeWhere: String,
    val noticeSource: String,
    val couldntLoadUpdate: String,
    val website: String,
    val couldntLoadService: String
)

internal fun stateStringsFor(language: String): StateStrings = when (language) {
    "fr" -> StateStrings(
        filterWhen = "Quand", filterType = "Type", resetFilters = "Réinitialiser les filtres",
        couldntLoadEvents = "Impossible de charger les événements.",
        couldntLoadUpdates = "Impossible de charger les actualités locales.",
        couldntLoadServices = "Impossible de charger les services.",
        couldntRefresh = "Actualisation impossible. Les informations affichées peuvent ne pas être à jour.",
        noUpdates = "Aucune actualité locale pour le moment.",
        noServices = "Aucun service répertorié pour le moment.",
        noticeWhen = "Quand", noticeWhere = "Où", noticeSource = "Source et détails",
        couldntLoadUpdate = "Impossible de charger cette actualité.",
        website = "Site web", couldntLoadService = "Impossible de charger ce service."
    )
    "de" -> StateStrings(
        filterWhen = "Wann", filterType = "Art", resetFilters = "Filter zurücksetzen",
        couldntLoadEvents = "Veranstaltungen konnten nicht geladen werden.",
        couldntLoadUpdates = "Lokale Hinweise konnten nicht geladen werden.",
        couldntLoadServices = "Dienste konnten nicht geladen werden.",
        couldntRefresh = "Aktualisierung nicht möglich. Die Angaben sind eventuell nicht aktuell.",
        noUpdates = "Derzeit keine lokalen Hinweise.",
        noServices = "Noch keine Dienste eingetragen.",
        noticeWhen = "Wann", noticeWhere = "Wo", noticeSource = "Quelle und Details",
        couldntLoadUpdate = "Dieser Hinweis konnte nicht geladen werden.",
        website = "Website", couldntLoadService = "Dieser Dienst konnte nicht geladen werden."
    )
    "es" -> StateStrings(
        filterWhen = "Cuándo", filterType = "Tipo", resetFilters = "Restablecer filtros",
        couldntLoadEvents = "No se pudieron cargar los eventos.",
        couldntLoadUpdates = "No se pudieron cargar los avisos locales.",
        couldntLoadServices = "No se pudieron cargar los servicios.",
        couldntRefresh = "No se pudo actualizar. Es posible que la información no esté al día.",
        noUpdates = "No hay avisos locales por ahora.",
        noServices = "Aún no hay servicios.",
        noticeWhen = "Cuándo", noticeWhere = "Dónde", noticeSource = "Fuente y detalles",
        couldntLoadUpdate = "No se pudo cargar este aviso.",
        website = "Sitio web", couldntLoadService = "No se pudo cargar este servicio."
    )
    "tr" -> StateStrings(
        filterWhen = "Ne zaman", filterType = "Tür", resetFilters = "Filtreleri sıfırla",
        couldntLoadEvents = "Etkinlikler yüklenemedi.",
        couldntLoadUpdates = "Yerel duyurular yüklenemedi.",
        couldntLoadServices = "Hizmetler yüklenemedi.",
        couldntRefresh = "Yenilenemedi. Gösterilen bilgiler güncel olmayabilir.",
        noUpdates = "Şu anda yerel duyuru yok.",
        noServices = "Henüz listelenmiş hizmet yok.",
        noticeWhen = "Ne zaman", noticeWhere = "Nerede", noticeSource = "Kaynak ve ayrıntılar",
        couldntLoadUpdate = "Bu duyuru yüklenemedi.",
        website = "Web sitesi", couldntLoadService = "Bu hizmet yüklenemedi."
    )
    else -> StateStrings(
        filterWhen = "When", filterType = "Type", resetFilters = "Reset filters",
        couldntLoadEvents = "Couldn't load events.",
        couldntLoadUpdates = "Couldn't load local updates.",
        couldntLoadServices = "Couldn't load services.",
        couldntRefresh = "Couldn't refresh. What's shown may be out of date.",
        noUpdates = "No local updates right now.",
        noServices = "No services listed yet.",
        noticeWhen = "When", noticeWhere = "Where", noticeSource = "Source and details",
        couldntLoadUpdate = "Couldn't load this update.",
        website = "Website", couldntLoadService = "Couldn't load this service."
    )
}
