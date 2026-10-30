package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.HomeArea

sealed interface HomeAction : BaseAction {
    data object LoadHomeData : HomeAction
    data class RetrySection(val section: HomeSection) : HomeAction
    data class SelectArea(val area: HomeArea) : HomeAction

    data class PlaceClicked(val id: String, val origin: String) : HomeAction
    data class EventClicked(val id: String) : HomeAction
    data class NoticeClicked(val id: String) : HomeAction
    /** [saved] is the state the visitor asked for (not a blind flip). */
    data class SetFavorite(val placeId: String, val saved: Boolean) : HomeAction
    /** Nearby card "Listen": opens the place's audio guide and starts playback. */
    data class ListenClicked(val placeId: String) : HomeAction

    data object SeeAllNearby : HomeAction
    data object SeeAllMustSee : HomeAction
    data object SeeAllInfoCenter : HomeAction
    data object SeeAllEvents : HomeAction
    data object OpenSearch : HomeAction
    data object OpenLanguagePicker : HomeAction
    data object OpenMyGuides : HomeAction

    data object RequestLocationPermission : HomeAction
    data class LocationPermissionResult(val granted: Boolean) : HomeAction
    data object DismissLocationPrompt : HomeAction
}
