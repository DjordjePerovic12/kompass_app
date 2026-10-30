package llc.bokadev.kompass.presentation.search

import llc.bokadev.kompass.domain.model.SearchHit
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class SearchEvent : BaseEvent {
    data object NavigateBack : SearchEvent()
    data object OnRetryClick : SearchEvent()
    data object OnShowMoreClick : SearchEvent()
    data object OnClearRecentClick : SearchEvent()
    data object OnBrowseExploreClick : SearchEvent()
    data class OnQueryChange(val value: String) : SearchEvent()

    /** A Recent row, a Try chip or the keyboard's search key: runs at once, without the debounce. */
    data class OnQuerySubmit(val value: String) : SearchEvent()

    /** Null = All. */
    data class OnKindChange(val kind: SearchKind?) : SearchEvent()
    data class OnResultClick(val hit: SearchHit) : SearchEvent()

    /** Emitted by the ViewModel on eventFlow: leaving for a result, put the keyboard away first. */
    data object HideKeyboard : SearchEvent()
}
