package llc.bokadev.kompass.presentation.home

import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.HomeArea
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/**
 * Home's sections load, fail and retry on their own ([sections]), each showing its loading or
 * failure inline under its title. So [isLoading] stays false (the masthead shows at once) and
 * [error] is never set.
 */
data class HomeState(
    override val isLoading: Boolean = false,
    override val error: BaseError? = null,
    val area: HomeArea = HomeArea.DEVICE,
    val sections: Map<HomeSection, SectionStatus> = HomeSection.entries.associateWith { SectionStatus.LOADING },
    val mustSeePlaces: List<Place> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val infoCenterNotices: List<InfoNotice> = emptyList(),
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    /** The browsing area's destination overview (editorial entry), shown apart from nearby attractions. */
    val areaOverview: Place? = null,
    val isUsingCurrentLocation: Boolean = false,
    val hasPremiumAccess: Boolean = false,
    /** Saved items, so the cards show their saved state. */
    val favoriteKeys: Set<FavoriteKey> = emptySet(),
    /** Business-driven, not pure UI: depends on permission status + whether already dismissed. */
    val showLocationPrompt: Boolean = false
) : BaseViewState(isLoading, error) {
    val effectiveArea: HomeArea get() = if (area == HomeArea.DEVICE && !isUsingCurrentLocation) HomeArea.OLD_TOWN else area
    fun withStatus(section: HomeSection, status: SectionStatus) = copy(sections = sections + (section to status))
}

enum class HomeSection { NEARBY, HIGHLIGHTS, NOTICES, EVENTS }
enum class SectionStatus { LOADING, READY, FAILED }
