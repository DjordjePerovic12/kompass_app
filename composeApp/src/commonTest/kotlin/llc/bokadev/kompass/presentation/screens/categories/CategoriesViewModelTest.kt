package llc.bokadev.kompass.presentation.screens.categories

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.testutil.NoOpAnalytics
import llc.bokadev.kompass.testutil.RecordingNavigator
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    @Test fun dynamicBitsComeFromRealData() = run(
        places = listOf(place("p1", "old_town", photo = "kotor.jpg"), place("p2", "perast", photo = "perast.jpg", lat = 42.4868, lng = 18.6982)),
        collection = listOf(Excursion("featured", place = place("p2", "perast", photo = "perast.jpg"))),
        finds = listOf(find(audio = mapOf("en" to "story.mp3"))),
        events = listOf(event("2099-10-12T18:00:00Z")),
        notices = listOf(notice("general"), notice("urgent"))
    ) { state ->
        assertEquals("kotor.jpg", state.heroPhoto)
        assertTrue(state.hasWorthTheDrive)
        assertEquals("perast.jpg", state.worthTheDrivePhoto) // Explicitly featured, regardless of coordinates.
        assertTrue(state.hasStories)
        assertTrue(state.storiesHaveAudio)
        assertEquals("2099-10-12T18:00:00Z", state.nextEventStart)
        assertTrue(state.hasImportantNotice)
    }

    @Test fun emptyCategoriesHideTheirCardsAndGeneralNoticesRaiseNoAlert() = run(
        places = listOf(place("p1", "old_town"), place("p2", "dobrota", lat = 42.4430, lng = 18.7630)),
        finds = emptyList(),
        events = emptyList(),
        notices = listOf(notice("general"))
    ) { state ->
        assertFalse(state.hasWorthTheDrive)
        assertFalse(state.hasStories)
        assertNull(state.nextEventStart)
        assertFalse(state.hasImportantNotice)
    }

    @Test fun failedFetchesNeverHideAnEntryPoint() = run(fail = true) { state ->
        assertTrue(state.hasWorthTheDrive)
        assertTrue(state.hasStories)
        assertNull(state.heroPhoto)
        assertNull(state.nextEventStart)
    }

    private fun run(
        places: List<Place> = emptyList(),
        collection: List<Excursion> = emptyList(),
        finds: List<LocalFind> = emptyList(),
        events: List<Event> = emptyList(),
        notices: List<InfoNotice> = emptyList(),
        fail: Boolean = false,
        assertions: (CategoriesState) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            fun <T> result(value: T): Resource<T, NetworkError, String?> = if (fail) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(value)
            val placeRepo = object : PlaceRepository {
                override suspend fun getActivePlaces() = result(places)
                override suspend fun getMustSeePlaces() = result(places)
                override suspend fun getPlacesByCategory(category: PlaceCategory) = result(places)
                override suspend fun getPlaceById(id: String) = result(places.first())
            }
            val vm = CategoriesViewModel(
                GetMustSeePlacesUseCase(placeRepo),
                object : ExcursionRepository {
                    override suspend fun getCollection() = result(ExcursionResult(collection))
                    override suspend fun getRelatedActivities(placeId: String): Resource<ExcursionResult<Experience>, NetworkError, String?> = error("unused")
                },
                GetLocalFindsUseCase(object : LocalFindRepository {
                    override suspend fun getLocalFinds() = result(finds)
                    override suspend fun getLocalFindById(id: String): Resource<LocalFind, NetworkError, String?> = error("unused")
                }),
                GetUpcomingEventsUseCase(object : EventRepository {
                    override suspend fun getEvents(dateFilter: String, eventType: String) = result(events)
                    override suspend fun getEventFilters() = result(emptyList<EventFilter>())
                    override suspend fun getUpcomingEvents() = result(events)
                    override suspend fun getEventById(id: String): Resource<Event, NetworkError, String?> = error("unused")
                }),
                GetCurrentInfoNoticesUseCase(object : InfoCenterRepository {
                    override suspend fun getCurrentNotices() = result(notices)
                    override suspend fun getNoticeById(id: String): Resource<InfoNotice, NetworkError, String?> = error("unused")
                }),
                RecordingNavigator(),
                LanguageSheetHost(),
                NoOpAnalytics()
            )
            advanceUntilIdle()
            assertions(vm.state.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun place(id: String, zone: String, photo: String? = null, lat: Double = 42.4247, lng: Double = 18.7712) = Place(
        id = id, cityId = "kotor", name = mapOf("en" to id), description = emptyMap(), longDescription = emptyMap(),
        localsTip = emptyMap(), category = PlaceCategory.SEE_AND_VISIT, subCategory = null, priceIndicator = null,
        latitude = lat, longitude = lng, zone = zone, tags = emptyList(), bestTime = BestTime.ANYTIME,
        estimatedDuration = null, openingHours = null, menuUrl = null, bookingPhone = null,
        photos = listOfNotNull(photo), audioFile = null, audioAccessTier = "free", detailAccessTier = "free",
        isMustSee = true, isActive = true, sortOrder = 0
    )

    private fun find(audio: Map<String, String>?) = LocalFind(
        id = "f", cityId = "kotor", title = mapOf("en" to "Story"), body = emptyMap(), audioFile = audio,
        location = null, latitude = null, longitude = null, photos = emptyList(), isActive = true, sortOrder = 0
    )

    private fun event(start: String) = Event(
        id = "e", name = mapOf("en" to "Concert"), description = emptyMap(), venue = emptyMap(),
        latitude = 0.0, longitude = 0.0, category = "music", startTime = start, endTime = null,
        price = null, ticketUrl = null, isRecurring = false, photos = emptyList()
    )

    private fun notice(priority: String) = InfoNotice(
        id = priority, cityId = "kotor", title = mapOf("en" to priority), shortDescription = emptyMap(), priority = priority
    )
}
