package llc.bokadev.kompass.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SearchResultType {
    PLACE,
    ACTIVITY,
    EVENT
}

@Serializable
enum class SearchFilterType {
    ALL,
    PLACE,
    ACTIVITY,
    EVENT
}

@Serializable
data class SearchResult(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val categoryLabel: String? = null,
    val zoneOrLocation: String? = null,
    val isMustSee: Boolean = false
) {
    fun favoriteKeyOrNull(): FavoriteKey? = when (type) {
        SearchResultType.PLACE -> FavoriteKey(FavoriteItemType.PLACE, id)
        SearchResultType.ACTIVITY -> FavoriteKey(FavoriteItemType.ACTIVITY, id)
        SearchResultType.EVENT -> null
    }

    fun matchesQueryLocally(query: String): Boolean {
        val normalizedQuery = query.normalizeSearchTerm()
        if (normalizedQuery.isBlank()) return true

        return buildList {
            add(title)
            add(subtitle.orEmpty())
            add(categoryLabel.orEmpty())
            add(zoneOrLocation.orEmpty())
        }.any { candidate ->
            candidate.normalizeSearchTerm().contains(normalizedQuery)
        }
    }
}

@Serializable
data class SearchCachePayload(
    val lastQuery: String,
    val language: String,
    val cachedAtEpochMs: Long,
    val results: List<SearchResult>
)

data class SearchResponse(
    val results: List<SearchResult>,
    val fromCache: Boolean,
    val offlineRequiresInternet: Boolean
)

fun String.normalizeSearchTerm(): String =
    lowercase()
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
