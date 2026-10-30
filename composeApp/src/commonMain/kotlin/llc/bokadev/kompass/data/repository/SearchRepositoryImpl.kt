package llc.bokadev.kompass.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.SearchCachePayload
import llc.bokadev.kompass.domain.model.SearchFilterType
import llc.bokadev.kompass.domain.model.SearchResponse
import llc.bokadev.kompass.domain.model.SearchResult
import llc.bokadev.kompass.domain.model.SearchResultType
import llc.bokadev.kompass.domain.repository.SearchRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Clock

@OptIn(ExperimentalTime::class)
class SearchRepositoryImpl(
    private val client: HttpClient,
    private val preferences: AppPreferences
) : SearchRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun search(
        query: String,
        language: String,
        type: SearchFilterType
    ): Result<SearchResponse> = runCatching {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return@runCatching SearchResponse(
                results = getCachedSearch(language)?.results.orEmpty(),
                fromCache = true,
                offlineRequiresInternet = false
            )
        }

        runCatching {
            val httpResponse = client.post("${functionsBaseUrl().trimEnd('/')}/search-content") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${supabaseAnonKey()}")
                header("apikey", supabaseAnonKey())
                setBody(
                    SearchRequestDto(
                        query = normalizedQuery,
                        lang = language,
                        type = type.apiValue
                    )
                )
            }
            if (!httpResponse.status.isSuccess()) {
                throw SearchServiceException(
                    IllegalStateException(
                        "Search endpoint returned ${httpResponse.status.value}: ${httpResponse.bodyAsText()}"
                    )
                )
            }
            val response = httpResponse.body<SearchResponseDto>()

            val results = response.results.map { it.toDomain() }
            cacheSearch(
                SearchCachePayload(
                    lastQuery = normalizedQuery,
                    language = language,
                    cachedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                    results = results
                )
            )

            SearchResponse(
                results = results,
                fromCache = false,
                offlineRequiresInternet = false
            )
        }.getOrElse { remoteError ->
            val cached = getCachedSearch(language)
            val filtered = cached?.results.orEmpty().filter { it.matchesQueryLocally(normalizedQuery) }
            if (filtered.isNotEmpty()) {
                SearchResponse(
                    results = filtered,
                    fromCache = true,
                    offlineRequiresInternet = false
                )
            } else {
                if (remoteError is ResponseException || remoteError is SearchServiceException) {
                    throw SearchServiceException(remoteError)
                }
                throw SearchOfflineMissException(remoteError)
            }
        }
    }

    override fun getCachedSearch(language: String): SearchCachePayload? {
        val raw = preferences.getString(CACHE_KEY_SEARCH_RESULTS) ?: return null
        return runCatching {
            json.decodeFromString(SearchCachePayload.serializer(), raw)
        }.getOrNull()?.takeIf { it.language == language }
    }

    private fun cacheSearch(payload: SearchCachePayload) {
        preferences.setString(
            CACHE_KEY_SEARCH_RESULTS,
            json.encodeToString(SearchCachePayload.serializer(), payload)
        )
    }

    private fun functionsBaseUrl(): String {
        return BuildKonfig.ANALYTICS_BACKEND_BASE_URL
            .takeIf { it.isNotBlank() }
            ?: "${BuildKonfig.SUPABASE_URL}/functions/v1"
    }

    private fun supabaseAnonKey(): String {
        return if (BuildKonfig.USE_LOCAL_SUPABASE && BuildKonfig.SUPABASE_ANON_KEY_LOCAL.isNotBlank()) {
            BuildKonfig.SUPABASE_ANON_KEY_LOCAL
        } else {
            BuildKonfig.SUPABASE_ANON_KEY
        }
    }

    companion object {
        const val CACHE_KEY_SEARCH_RESULTS = "offline_cache_last_search_results_v1"
    }
}

class SearchOfflineMissException(cause: Throwable) : Exception(cause)

class SearchServiceException(cause: Throwable) : Exception(cause)

private val SearchFilterType.apiValue: String
    get() = when (this) {
        SearchFilterType.ALL -> "all"
        SearchFilterType.PLACE -> "place"
        SearchFilterType.ACTIVITY -> "activity"
        SearchFilterType.EVENT -> "event"
    }

@Serializable
private data class SearchRequestDto(
    val query: String,
    val lang: String,
    val type: String
)

@Serializable
private data class SearchResponseDto(
    val results: List<SearchResultDto>
)

@Serializable
private data class SearchResultDto(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("category_label") val categoryLabel: String? = null,
    @SerialName("zone_or_location") val zoneOrLocation: String? = null,
    @SerialName("is_must_see") val isMustSee: Boolean = false
) {
    fun toDomain(): SearchResult = SearchResult(
        id = id,
        type = when (type.lowercase()) {
            "place" -> SearchResultType.PLACE
            "activity" -> SearchResultType.ACTIVITY
            else -> SearchResultType.EVENT
        },
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        latitude = latitude,
        longitude = longitude,
        categoryLabel = categoryLabel,
        zoneOrLocation = zoneOrLocation,
        isMustSee = isMustSee
    )
}
