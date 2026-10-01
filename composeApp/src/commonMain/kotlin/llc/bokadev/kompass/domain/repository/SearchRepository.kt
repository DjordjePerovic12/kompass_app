package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.SearchCachePayload
import llc.bokadev.kompass.domain.model.SearchFilterType
import llc.bokadev.kompass.domain.model.SearchResponse

interface SearchRepository {
    suspend fun search(
        query: String,
        language: String,
        type: SearchFilterType = SearchFilterType.ALL
    ): Result<SearchResponse>

    fun getCachedSearch(language: String): SearchCachePayload?
}
