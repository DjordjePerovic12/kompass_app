package llc.bokadev.kompass.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.core.utils.networking.map
import llc.bokadev.kompass.domain.model.SearchIndex
import llc.bokadev.kompass.domain.model.SearchIndexItem
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.domain.model.SearchSource
import llc.bokadev.kompass.domain.model.SearchTag
import llc.bokadev.kompass.domain.model.SearchVocabulary
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.domain.repository.PlaceRepository

/**
 * Builds the local search index from cached content (places, activities, stories, Essentials,
 * upcoming events, utility map points, services, current local updates). Each source loads
 * independently: failed ones are reported in [SearchIndex.failedSources] so the screen can say
 * results may be incomplete. The index fails only if every source does, which means nothing was
 * ever downloaded ("Search isn't ready yet").
 */
class BuildSearchIndexUseCase(
    private val placeRepository: PlaceRepository,
    private val getActivities: GetActivitiesUseCase,
    private val getLocalFinds: GetLocalFindsUseCase,
    private val getEssentials: GetEssentialsUseCase,
    private val getUpcomingEvents: GetUpcomingEventsUseCase,
    private val getUtilities: GetUtilitiesUseCase,
    private val getServices: GetServicesUseCase,
    private val getCurrentNotices: GetCurrentInfoNoticesUseCase,
    private val vocabulary: SearchVocabulary
) {
    suspend operator fun invoke(lang: String): Resource<SearchIndex, NetworkError, String?> = coroutineScope {
        fun terms(vararg tags: SearchTag?) = tags.filterNotNull().flatMap { vocabulary.terms(it, lang) }.distinct()

        val places = async {
            placeRepository.getActivePlaces().map { list ->
                list.map { p ->
                    val subtype = p.subCategory?.normalizedTag()
                    SearchIndexItem(
                        SearchKind.PLACE, p.id, p.localizedName(lang), aliases = alias(p.localizedName(lang), p.localizedName("en")), area = p.zone?.titleCase()?.ifBlank { null }, photo = p.photos.firstOrNull(),
                        placeCategory = p.category, subtype = p.subCategory?.takeIf { it.isNotBlank() }?.titleCase(),
                        keywords = terms(SearchTag.Place(p.category), subtype?.let(SearchTag::Subtype))
                    )
                }
            }
        }
        val activities = async {
            getActivities().map { list ->
                list.map { a ->
                    SearchIndexItem(
                        SearchKind.ACTIVITY, a.id, a.localizedName(lang), aliases = alias(a.localizedName(lang), a.localizedName("en")), area = a.localizedLocation(lang).ifBlank { null },
                        photo = a.photos.firstOrNull(), subtype = a.category?.titleCase(),
                        keywords = terms(a.category?.normalizedTag()?.let(SearchTag::Subtype))
                    )
                }
            }
        }
        val stories = async {
            getLocalFinds().map { list ->
                list.map { s -> SearchIndexItem(SearchKind.STORY, s.id, s.localizedTitle(lang), aliases = alias(s.localizedTitle(lang), s.localizedTitle("en")), area = s.localizedLocation(lang).ifBlank { null }, photo = s.photos.firstOrNull()) }
            }
        }
        val essentials = async {
            getEssentials().map { list ->
                list.map { e ->
                    SearchIndexItem(
                        SearchKind.ESSENTIAL, e.id, e.localizedTitle(lang), aliases = alias(e.localizedTitle(lang), e.localizedTitle("en")), essentialCategory = e.category,
                        keywords = terms(SearchTag.Essential(e.category))
                    )
                }
            }
        }
        val events = async {
            getUpcomingEvents().map { list ->
                list.map { ev ->
                    SearchIndexItem(
                        SearchKind.EVENT, ev.id, ev.localizedName(lang), aliases = alias(ev.localizedName(lang), ev.localizedName("en")), area = ev.localizedVenue(lang).ifBlank { null },
                        photo = ev.photos.firstOrNull(), eventStart = ev.startTime
                    )
                }
            }
        }
        val utilities = async {
            getUtilities().map { list ->
                // One entry per category (opens its map), found by category and related words…
                val categories = list.groupBy { it.category }.map { (category, points) ->
                    val words = terms(SearchTag.Utility(category))
                    SearchIndexItem(
                        SearchKind.UTILITY, utilityCategoryId(category), words.firstOrNull() ?: category.name,
                        utilityCategory = category, keywords = words.drop(1), pointCount = points.size
                    )
                }
                // …and each point, found only by its own name or address (opens the map on it).
                categories + list.map { u ->
                    SearchIndexItem(
                        SearchKind.UTILITY, u.id, u.name.trim(), area = u.address?.trim()?.ifBlank { null },
                        utilityCategory = u.category, matchesByMeaning = false
                    )
                }
            }
        }
        val services = async {
            getServices().map { list ->
                list.map { sv ->
                    SearchIndexItem(SearchKind.SERVICE, sv.id, sv.localizedName(lang), aliases = alias(sv.localizedName(lang), sv.localizedName("en")), area = sv.localizedLocation(lang)?.ifBlank { null })
                }
            }
        }
        val notices = async {
            getCurrentNotices().map { list ->
                list.map { n ->
                    SearchIndexItem(
                        SearchKind.NOTICE, n.id, n.localizedTitle(lang), aliases = alias(n.localizedTitle(lang), n.localizedTitle("en")), area = n.localizedLocation(lang).ifBlank { null },
                        photo = n.imageUrl
                    )
                }
            }
        }

        val results: Map<SearchSource, Resource<List<SearchIndexItem>, NetworkError, String?>> = mapOf(
            SearchSource.PLACES to places.await(),
            SearchSource.ACTIVITIES to activities.await(),
            SearchSource.STORIES to stories.await(),
            SearchSource.ESSENTIALS to essentials.await(),
            SearchSource.EVENTS to events.await(),
            SearchSource.UTILITIES to utilities.await(),
            SearchSource.SERVICES to services.await(),
            SearchSource.NOTICES to notices.await()
        )
        val failed = results.filterValues { it is Resource.Error }.keys
        if (failed.size == results.size) {
            results.values.first() as? Resource.Error<NetworkError, String?> ?: Resource.Error(NetworkError.UNKNOWN, "No content")
        } else {
            val items = results.values.mapNotNull { it.getOrNull() }.flatten().filter { it.name.isNotBlank() }
            Resource.Success(SearchIndex(items, vocabulary.related(lang), failed))
        }
    }

    private fun utilityCategoryId(category: UtilityCategory) = "$UTILITY_CATEGORY_PREFIX${category.name}"

    /** The English name as a fallback match when the app language shows a different one. */
    private fun alias(name: String, english: String): List<String> =
        if (english.isNotBlank() && !english.equals(name, ignoreCase = true)) listOf(english) else emptyList()

    private fun String.normalizedTag(): String? =
        lowercase().replace('_', ' ').replace('-', ' ').split(' ').filter { it.isNotBlank() }.joinToString(" ").ifBlank { null }

    private fun String.titleCase(): String =
        replace('_', ' ').replace('-', ' ').split(' ').filter { it.isNotBlank() }
            .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercaseChar() } }
}

/** Search index id prefix of a whole utility category entry (not a CMS id). */
const val UTILITY_CATEGORY_PREFIX = "utility-category:"
