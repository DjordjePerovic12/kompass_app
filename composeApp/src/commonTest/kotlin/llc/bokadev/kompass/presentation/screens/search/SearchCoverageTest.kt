package llc.bokadev.kompass.presentation.screens.search

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import llc.bokadev.kompass.core.utils.SearchVocabularyTable
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.Screen

/** Step 10 search: category words, the reviewed vocabulary, new source types, partial availability. */
class SearchCoverageTest {

    private val vocabulary = SearchVocabularyTable()

    // --- Real builder over fake repositories (CMS-shaped content, Oct 2026) ---

    @Test fun aRestaurantQueryFindsEatAndDrinkPlacesWithoutTheWordInTheirTitle() = runTest {
        val index = build().getOrThrow()
        val hits = searchIndex("restaurant", index)
        assertEquals(setOf("galion", "pub"), hits.map { it.item.id }.toSet())
        assertTrue(hits.all { it.score == 4 && it.start == -1 }, "category words never highlight the title")
        // Subtype words too, with a prefix: "seaf" → the seafood place only.
        assertEquals(listOf("galion"), searchIndex("seaf", index).map { it.item.id })
        assertEquals(listOf("tryphon"), searchIndex("church", index).map { it.item.id })
    }

    @Test fun categoryWordsAreLocalizedAndEnglishStillWorks() = runTest {
        val fr = build(lang = "fr").getOrThrow()
        assertEquals(listOf("tryphon"), searchIndex("eglise", fr).map { it.item.id }, "accent-insensitive French subtype")
        assertEquals(listOf("tryphon"), searchIndex("cathedral", fr).map { it.item.id }, "English typed in a French app")
        assertEquals(setOf("galion", "pub"), searchIndex("restaurant", fr).map { it.item.id }.toSet())
    }

    @Test fun practicalQueriesReachExistingContent() = runTest {
        val index = build().getOrThrow()
        // "bus": the title itself (word prefix) beats the transport category word on taxi.
        assertEquals(listOf("bus_routes", "taxi"), searchIndex("bus", index).map { it.item.id })
        // "airport": no airport content; the vocabulary leads to taxi and the bus station.
        val airport = searchIndex("airport", index)
        assertEquals(setOf("taxi", "bus_routes"), airport.map { it.item.id }.toSet())
        assertTrue(airport.all { it.score == 5 })
        // "payment": the cash essential and ONE "Cash & ATMs" map entry, not every ATM.
        val payment = searchIndex("payment", index).map { it.item.id }
        assertEquals(listOf("cash_and_card", "${UTILITY_CATEGORY_PREFIX}ATM"), payment)
        // "pharmacy" finds the pharmacy category (the point's name is a brand), with its count.
        val pharmacy = searchIndex("pharmacy", index).single().item
        assertEquals("${UTILITY_CATEGORY_PREFIX}PHARMACY", pharmacy.id)
        assertEquals(1, pharmacy.pointCount)
        assertEquals(Screen.UtilityMapScreen(UtilityCategory.PHARMACY.name, null), routeFor(pharmacy))
    }

    @Test fun newTypesOpenTheirOwnDestinationsById() = runTest {
        val index = build().getOrThrow()
        val atm = searchIndex("nlb", index).single().item
        assertEquals(SearchKind.UTILITY, atm.kind)
        assertEquals(Screen.UtilityMapScreen(UtilityCategory.ATM.name, "atm-nlb"), routeFor(atm))
        assertEquals(Screen.ServiceDetailScreen("svc"), routeFor(searchIndex("car rental", index).single().item))
        assertEquals(Screen.InfoCenterDetailScreen("water"), routeFor(searchIndex("water", index).single().item))
        assertEquals(Screen.UtilityMapScreen(ALL_CATEGORIES, "x"), routeFor(SearchIndexItem(SearchKind.UTILITY, "x", "X")))
    }

    @Test fun editorialCollectionsAndOverviewsAreNotSearchWords() {
        assertTrue(vocabulary.terms(SearchTag.Subtype("worth the drive"), "en").isEmpty())
        assertTrue(vocabulary.terms(SearchTag.Subtype("destination overview"), "en").isEmpty())
        // Unknown CMS subtypes still match their own words.
        assertEquals(listOf("lighthouse"), vocabulary.terms(SearchTag.Subtype("lighthouse"), "en"))
    }

    @Test fun aFailedSourceIsReportedNotHidden() = runTest {
        val index = build(utilitiesFail = true).getOrThrow()
        assertEquals(setOf(SearchSource.UTILITIES), index.failedSources)
        assertFalse(index.isComplete)
        assertTrue(searchIndex("nlb", index).isEmpty())
        assertTrue(build().getOrThrow().isComplete)
    }

    @Test fun everySourceFailingIsNotReady() = runTest {
        assertTrue(build(allFail = true).isFailure)
    }

    @Test fun rankingIsDeterministicAcrossBuilds() = runTest {
        val a = searchIndex("ta", build().getOrThrow()).map { it.item.id to it.score }
        val b = searchIndex("ta", build().getOrThrow()).map { it.item.id to it.score }
        assertEquals(a, b)
    }

    // --- Fixtures ---

    private suspend fun build(lang: String = "en", utilitiesFail: Boolean = false, allFail: Boolean = false): Result<SearchIndex> {
        fun <T> r(value: T): Result<T> = if (allFail) Result.failure(IllegalStateException("offline")) else Result.success(value)
        val places = object : PlaceRepository {
            override suspend fun getActivePlaces() = r(listOf(
                place("galion", "Galion", PlaceCategory.EAT_AND_DRINK, "seafood"),
                place("pub", "Old Winery", PlaceCategory.EAT_AND_DRINK, "pub"),
                place("tryphon", "Saint Tryphon", PlaceCategory.SEE_AND_VISIT, "cathedral"),
                place("kotor", "Kotor", PlaceCategory.SEE_AND_VISIT, "destination_overview")
            ))
            override suspend fun getMustSeePlaces() = r(emptyList<Place>())
            override suspend fun getPlacesByCategory(category: PlaceCategory) = r(emptyList<Place>())
            override suspend fun getPlaceById(id: String) = Result.failure<Place>(NotImplementedError())
        }
        val experiences = object : ExperienceRepository {
            override suspend fun getActivities() = r(emptyList<Experience>())
            override suspend fun getActivityById(id: String) = Result.failure<Experience>(NotImplementedError())
        }
        val stories = object : LocalFindRepository {
            override suspend fun getLocalFinds() = r(emptyList<LocalFind>())
            override suspend fun getLocalFindById(id: String) = Result.failure<LocalFind>(NotImplementedError())
        }
        val essentials = object : EssentialRepository {
            override suspend fun getEssentials() = r(listOf(
                essential("taxi", EssentialCategory.TRANSPORT, "Taxi"),
                essential("bus_routes", EssentialCategory.TRANSPORT, "Main Bus Station"),
                essential("cash_and_card", EssentialCategory.TIPS, "Cash and car paying")
            ))
        }
        val events = object : EventRepository {
            override suspend fun getEvents(dateFilter: String, eventType: String) = r(emptyList<Event>())
            override suspend fun getEventFilters() = r(emptyList<EventFilter>())
            override suspend fun getUpcomingEvents() = r(emptyList<Event>())
            override suspend fun getEventById(id: String) = Result.failure<Event>(NotImplementedError())
        }
        val utilities = object : UtilityRepository {
            override suspend fun getUtilities() = if (utilitiesFail) Result.failure(IllegalStateException("offline")) else r(listOf(
                Utility("atm-nlb", "NLB Banka", UtilityCategory.ATM, 42.42, 18.77, "Stari grad", null),
                Utility("ph-1", "Benu", UtilityCategory.PHARMACY, 42.43, 18.77, "Škaljari", null)
            ))
        }
        val services = object : ServiceRepository {
            override suspend fun getServices() = r(listOf(Service("svc", mapOf("en" to "Green Motion Car Rental"), emptyMap(), mapOf("en" to "Kotor"))))
            override suspend fun getServiceById(id: String) = Result.failure<Service>(NotImplementedError())
        }
        val notices = object : InfoCenterRepository {
            override suspend fun getCurrentNotices() = r(listOf(InfoNotice("water", "kotor", mapOf("en" to "Water outage"), emptyMap())))
            override suspend fun getNoticeById(id: String) = Result.failure<InfoNotice>(NotImplementedError())
        }
        return BuildSearchIndexUseCase(
            places, GetActivitiesUseCase(experiences), GetLocalFindsUseCase(stories), GetEssentialsUseCase(essentials),
            GetUpcomingEventsUseCase(events), GetUtilitiesUseCase(utilities), GetServicesUseCase(services),
            GetCurrentInfoNoticesUseCase(notices), vocabulary
        )(lang)
    }

    private fun place(id: String, name: String, category: PlaceCategory, sub: String?) = Place(
        id = id, cityId = "kotor", name = mapOf("en" to name), description = emptyMap(), longDescription = emptyMap(),
        localsTip = emptyMap(), category = category, subCategory = sub, priceIndicator = null,
        latitude = 42.42, longitude = 18.77, zone = "old_town", tags = emptyList(), bestTime = BestTime.ANYTIME,
        estimatedDuration = null, openingHours = null, menuUrl = null, bookingPhone = null, photos = emptyList(),
        audioFile = null, audioAccessTier = "free", detailAccessTier = "free", isMustSee = false, isActive = true, sortOrder = 0
    )

    private fun essential(key: String, category: EssentialCategory, title: String) =
        CityEssential(id = key, key = key, category = category, title = mapOf("en" to title), content = emptyMap())
}
