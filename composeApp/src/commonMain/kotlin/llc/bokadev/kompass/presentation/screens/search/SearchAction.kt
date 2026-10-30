package llc.bokadev.kompass.presentation.screens.search

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.SearchHit
import llc.bokadev.kompass.domain.model.SearchKind

sealed interface SearchAction : BaseAction {
    data class QueryChanged(val value: String) : SearchAction
    /** A Recent row, a Try chip or the keyboard's search key: runs at once, without the debounce. */
    data class RunQuery(val value: String) : SearchAction
    data class SelectKind(val kind: SearchKind?) : SearchAction
    data class Open(val hit: SearchHit) : SearchAction
    data object ShowMore : SearchAction
    data object ClearRecent : SearchAction
    data object Retry : SearchAction
    data object BrowseExplore : SearchAction
    data object Back : SearchAction
}
