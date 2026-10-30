package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.HomeArea

data class HomeState(
    val area: HomeArea = HomeArea.DEVICE,
    val sections: Map<HomeSection, SectionStatus> = HomeSection.entries.associateWith { SectionStatus.LOADING },
    override val isLoading: Boolean = false,
    override val error: UiText? = null,
    val mustSeePlaces: List<Place> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val infoCenterNotices: List<InfoNotice> = emptyList(),
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    /** The browsing area's destination overview (editorial entry), shown apart from nearby attractions. */
    val areaOverview: Place? = null,
    val isUsingCurrentLocation: Boolean = false,
    val hasPremiumAccess: Boolean = false,
    /** Business-driven, not pure UI: depends on permission status + whether already dismissed. */
    val showLocationPrompt: Boolean = false
) : BaseViewState {
    val effectiveArea: HomeArea get() = if (area == HomeArea.DEVICE && !isUsingCurrentLocation) HomeArea.OLD_TOWN else area
    fun withStatus(section: HomeSection, status: SectionStatus) = copy(sections = sections + (section to status))
}

enum class HomeSection { NEARBY, HIGHLIGHTS, NOTICES, EVENTS }
enum class SectionStatus { LOADING, READY, FAILED }
