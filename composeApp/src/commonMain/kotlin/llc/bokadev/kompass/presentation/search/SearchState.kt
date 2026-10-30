package llc.bokadev.kompass.presentation.search

import llc.bokadev.kompass.domain.model.SEARCH_LIMIT
import llc.bokadev.kompass.domain.model.SEARCH_MIN_CHARS
import llc.bokadev.kompass.domain.model.SearchHit
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** Whether the local index exists. NotReady = nothing cached yet (first launch, offline). */
enum class SearchIndexStatus { LOADING, READY, NOT_READY }

/**
 * Search never loads or fails as a whole: the field is usable at once and the index's progress is
 * [indexStatus] / [isIncomplete], shown inline. So [isLoading] stays false and [error] null.
 */
data class SearchState(
    override val isLoading: Boolean = false,
    override val error: BaseError? = null,
    val query: String = "",
    /** The query the current [hits] were computed for (lags [query] by the debounce). */
    val searchedQuery: String = "",
    /** All hits for [searchedQuery], across types. */
    val hits: List<SearchHit> = emptyList(),
    /** Null = All. Reset to All whenever the query changes. */
    val selectedKind: SearchKind? = null,
    val visibleLimit: Int = SEARCH_LIMIT,
    val recent: List<String> = emptyList(),
    val indexStatus: SearchIndexStatus = SearchIndexStatus.LOADING,
    /** Some sources couldn't load (and nothing was cached): results, or "nothing", may be incomplete. */
    val isIncomplete: Boolean = false
) : BaseViewState(isLoading, error) {
    val isSearching: Boolean get() = searchedQuery.trim().length >= SEARCH_MIN_CHARS
    /** Counts per type, only for types with at least one hit, in display order. */
    val kindCounts: List<Pair<SearchKind, Int>>
        get() = SearchKind.entries.mapNotNull { kind -> hits.count { it.item.kind == kind }.takeIf { it > 0 }?.let { kind to it } }
    val filtered: List<SearchHit> get() = selectedKind?.let { k -> hits.filter { it.item.kind == k } } ?: hits
    val visible: List<SearchHit> get() = filtered.take(visibleLimit)
    val hasMore: Boolean get() = filtered.size > visibleLimit
}
