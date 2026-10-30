package llc.bokadev.kompass.presentation.explore

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class ExploreEvent : BaseEvent {
    data class OnDestinationClick(val destination: ExploreDestination) : ExploreEvent()
    data object OnSearchClick : ExploreEvent()
    data object OnLanguageClick : ExploreEvent()
}
