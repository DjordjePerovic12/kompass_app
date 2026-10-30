package llc.bokadev.kompass.presentation.home

import llc.bokadev.kompass.domain.model.HomeArea
import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class HomeEvent : BaseEvent {
    /** Reloads every section; reserved for pull-to-refresh, not sent by the UI yet. */
    data object OnRefresh : HomeEvent()
    data class OnRetrySectionClick(val section: HomeSection) : HomeEvent()
    data class OnAreaSelect(val area: HomeArea) : HomeEvent()

    data class OnPlaceClick(val id: String, val origin: String) : HomeEvent()
    data class OnEventClick(val id: String) : HomeEvent()
    data class OnNoticeClick(val id: String) : HomeEvent()

    /** [saved] is the state the visitor asked for (not a blind flip). */
    data class OnFavoriteChange(val placeId: String, val saved: Boolean) : HomeEvent()

    /** Nearby card "Listen": opens the place's audio guide and starts playback. */
    data class OnListenClick(val placeId: String) : HomeEvent()

    data object OnSeeAllNearbyClick : HomeEvent()
    /** Reserved: the must-see "See all" link is hidden for now (spec A4). */
    data object OnSeeAllMustSeeClick : HomeEvent()
    data object OnSeeAllInfoCenterClick : HomeEvent()
    data object OnSeeAllEventsClick : HomeEvent()
    data object OnSearchClick : HomeEvent()
    data object OnLanguageClick : HomeEvent()
    /** Reserved for a "My guides" entry point, not sent by the UI yet. */
    data object OnMyGuidesClick : HomeEvent()

    /** The visitor confirmed the location-education dialog. */
    data object OnLocationPromptContinueClick : HomeEvent()
    data object OnLocationPromptDismiss : HomeEvent()
    data class OnLocationPermissionResult(val granted: Boolean) : HomeEvent()

    /** Emitted by the ViewModel on eventFlow: the platform permission prompt should fire now. */
    data object TriggerLocationPermissionRequest : HomeEvent()
}
