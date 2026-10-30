package llc.bokadev.kompass

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.first
import llc.bokadev.kompass.core.presentation.mvi.NavigationCommand
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.testutil.RecordingNavigator
import llc.bokadev.kompass.core.utils.*
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.presentation.screens.home.*
import llc.bokadev.kompass.presentation.shared.dotWindow

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRedesignTest {
    @Test fun normalizedZonesNeverUseSubstringMatching() {
        assertTrue(HomeArea.OLD_TOWN.matchesZone(" Old Town "))
        assertTrue(HomeArea.OLD_TOWN.matchesZone("KOTOR-OLD-TOWN"))
        assertTrue(HomeArea.DOBROTA.matchesZone(" DOBROTA "))
        assertTrue(HomeArea.PERAST.matchesZone("perast"))
        assertFalse(HomeArea.PERAST.matchesZone("near_perast"))
        assertFalse(HomeArea.OLD_TOWN.matchesZone(null))
        assertFalse(HomeArea.DEVICE.matchesZone("old_town"))
    }

    @Test fun areaPreferencesAreBackwardCompatible() {
        assertEquals(HomeArea.DEVICE, HomeArea.restore(null))
        assertEquals(HomeArea.DEVICE, HomeArea.restore("retired_value"))
        HomeArea.entries.forEach { assertEquals(it, HomeArea.restore(it.preferenceValue)) }
        assertNotEquals(HomeArea.PERAST.sectionTitle(appStringsFor("en")), appStringsFor("en").nearbyYou)
    }

    @Test fun failedEventsDoNotSuppressNearbyAndRetryIsIndependent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val data = TestPlaces()
            val events = TestEvents()
            val prefs = TestPreferences()
            val location = TestLocation()
            val vm = model(data, events, prefs, location)
            advanceUntilIdle()
            assertEquals(SectionStatus.FAILED, vm.state.value.sections[HomeSection.EVENTS])
            assertEquals(SectionStatus.READY, vm.state.value.sections[HomeSection.NEARBY])
            assertEquals(listOf("nearest", "second"), vm.state.value.nearbyPlaces.map { it.place.id })
            assertTrue(vm.state.value.isUsingCurrentLocation)
            val placeCalls = data.activeCalls
            events.fail = false
            vm.onAction(HomeAction.RetrySection(HomeSection.EVENTS))
            advanceUntilIdle()
            assertEquals(SectionStatus.READY, vm.state.value.sections[HomeSection.EVENTS])
            assertEquals(placeCalls, data.activeCalls)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun manualAreaPersistsAndDoesNotAskForLocation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val prefs = TestPreferences().apply { setString(HomeArea.PREFERENCE_KEY, "perast") }
            val location = TestLocation()
            val vm = model(TestPlaces(), TestEvents(), prefs, location)
            advanceUntilIdle()
            assertEquals(listOf("perast"), vm.state.value.nearbyPlaces.map { it.place.id })
            assertEquals(0, location.requests)
            assertFalse(vm.state.value.isUsingCurrentLocation)
            vm.onAction(HomeAction.SelectArea(HomeArea.DOBROTA))
            advanceUntilIdle()
            assertEquals("dobrota", prefs.getString(HomeArea.PREFERENCE_KEY))
            assertEquals(HomeArea.DOBROTA, vm.state.value.effectiveArea)
            assertTrue(vm.state.value.nearbyPlaces.isEmpty())
            vm.onAction(HomeAction.SelectArea(HomeArea.DEVICE))
            advanceUntilIdle()
            assertEquals(1, location.requests)
            assertTrue(vm.state.value.isUsingCurrentLocation)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun deniedLocationUsesLabeledOldTownWithoutRequestingPermission() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val location = TestLocation(permission = false)
            val vm = model(TestPlaces(), TestEvents(), TestPreferences(), location)
            advanceUntilIdle()
            assertEquals(0, location.requests)
            assertEquals(HomeArea.DEVICE, vm.state.value.area)
            assertEquals(HomeArea.OLD_TOWN, vm.state.value.effectiveArea)
            assertFalse(vm.state.value.isUsingCurrentLocation)
            assertEquals(listOf("second", "nearest"), vm.state.value.nearbyPlaces.map { it.place.id })
        } finally { Dispatchers.resetMain() }
    }

    @Test fun nearbyPlacesUseCaseAppliesCategoryAndRadiusFilterInOneSortedPass() = runTest {
        val places = TestPlaces()
        val result = GetNearbyPlacesUseCase(places)(
            origin = GeoPoint(42.4246, 18.7712),
            category = PlaceCategory.SEE_AND_VISIT,
            maxDistanceKm = 1.0
        ).getOrNull()!!
        // "restaurant" is EAT_AND_DRINK (filtered by category) and "perast" is >1km away
        // (filtered by radius) — only the two SEE_AND_VISIT old-town places remain, sorted.
        assertEquals(listOf("nearest", "second"), result.map { it.place.id })
    }

    @Test fun nearbyFallbackUseCaseZoneMatchesWithoutFabricatingDistance() = runTest {
        val places = TestPlaces()
        val result = GetNearbyFallbackPlacesUseCase(places)(
            category = PlaceCategory.SEE_AND_VISIT,
            matchesZone = HomeArea.PERAST::matchesZone,
            limit = 4
        ).getOrNull()!!
        assertEquals(listOf("perast"), result.map { it.place.id })
        assertTrue(result.all { it.distanceKm == 0.0 })
    }

    @Test fun unavailableLocationFallsBackButDoesNotChangePreference() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val prefs = TestPreferences()
            val vm = model(TestPlaces(), TestEvents(), prefs, TestLocation(fail = true))
            advanceUntilIdle()
            assertEquals(SectionStatus.READY, vm.state.value.sections[HomeSection.NEARBY])
            assertEquals(HomeArea.OLD_TOWN, vm.state.value.effectiveArea)
            assertEquals(HomeArea.DEVICE, vm.state.value.area)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun listenOpensPlaceDetailWithAutoplay() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val navigator = RecordingNavigator()
            val vm = model(TestPlaces(), TestEvents(), TestPreferences(), TestLocation(), navigator)
            advanceUntilIdle()
            vm.onAction(HomeAction.ListenClicked("nearest"))
            val command = navigator.recorded.first()
            assertEquals(Screen.PlaceDetailScreen("nearest", autoplay = true), (command as NavigationCommand.NavigateTo).route)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun destinationOverviewsAreOfferedEditoriallyNotAsNearbyAttractions() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val prefs = TestPreferences()
            val vm = model(TestPlaces(), TestEvents(), prefs, TestLocation())
            advanceUntilIdle()
            assertTrue(vm.state.value.nearbyPlaces.none { it.place.isDestinationOverview })
            assertEquals("kotor_overview", vm.state.value.areaOverview?.id)
            vm.onAction(HomeAction.SelectArea(HomeArea.PERAST))
            advanceUntilIdle()
            assertEquals(listOf("perast"), vm.state.value.nearbyPlaces.map { it.place.id })
            assertEquals("perast_overview", vm.state.value.areaOverview?.id)
            vm.onAction(HomeAction.SelectArea(HomeArea.DOBROTA))
            advanceUntilIdle()
            assertNull(vm.state.value.areaOverview, "no overview in the CMS → none shown, never a substitute")
        } finally { Dispatchers.resetMain() }
    }

    @Test fun seeAllOpensTheSharedListWithTheManualArea() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val navigator = RecordingNavigator()
            val prefs = TestPreferences().apply { setString(HomeArea.PREFERENCE_KEY, "perast") }
            val vm = model(TestPlaces(), TestEvents(), prefs, TestLocation(), navigator)
            advanceUntilIdle()
            vm.onAction(HomeAction.SeeAllNearby)
            vm.onAction(HomeAction.SelectArea(HomeArea.DEVICE))
            vm.onAction(HomeAction.SeeAllNearby)
            vm.onAction(HomeAction.SeeAllMustSee)
            val routes = navigator.recorded.take(3).map { (it as NavigationCommand.NavigateTo).route }
            assertEquals(
                listOf(
                    Screen.CategoryListScreen("PLACES", area = "perast"),
                    Screen.CategoryListScreen("PLACES", area = null),
                    Screen.CategoryListScreen("MUST_SEE")
                ),
                routes
            )
        } finally { Dispatchers.resetMain() }
    }

    @Test fun nearbyDotWindowShowsAllUpToFiveThenSlidesAroundCurrent() {
        assertEquals(0 until 1, dotWindow(current = 0, total = 1))
        assertEquals(0 until 5, dotWindow(current = 4, total = 5))
        assertEquals(0 until 5, dotWindow(current = 0, total = 6))
        assertEquals(1 until 6, dotWindow(current = 5, total = 6))
        assertEquals(0 until 5, dotWindow(current = 2, total = 12))
        assertEquals(4 until 9, dotWindow(current = 6, total = 12))
        assertEquals(7 until 12, dotWindow(current = 11, total = 12))
    }

    @Test fun areaLabelUppercaseRespectsTurkishDottedI() {
        assertEquals("PERAST", "Perast".uppercaseFor("en"))
        assertEquals("BİR İŞ", "bir iş".uppercaseFor("tr"))
        assertEquals("BIR IS", "bir is".uppercaseFor("de"))
    }

    private fun model(
        places: TestPlaces,
        events: TestEvents,
        prefs: TestPreferences,
        location: TestLocation,
        navigator: RecordingNavigator = RecordingNavigator()
    ) =
        HomeViewModel(
            getMustSeePlaces = GetMustSeePlacesUseCase(places),
            getUpcomingEvents = GetUpcomingEventsUseCase(events),
            getCurrentInfoNotices = GetCurrentInfoNoticesUseCase(object : InfoCenterRepository {
                override suspend fun getCurrentNotices() = Resource.Success(emptyList<InfoNotice>())
                override suspend fun getNoticeById(id: String): Resource<InfoNotice, NetworkError, String?> = error("Unused")
            }),
            getNearbyPlaces = GetNearbyPlacesUseCase(places),
            getNearbyFallbackPlaces = GetNearbyFallbackPlacesUseCase(places),
            getDestinationOverview = GetDestinationOverviewUseCase(places),
            userLocationProvider = location,
            preferences = prefs,
            analytics = TestAnalytics(),
            favorites = TestFavorites(),
            navigator = navigator,
            languageSheet = LanguageSheetHost()
        )

    private class TestAnalytics : AnalyticsRepository {
        override fun trackScreenView(screenName: String) = Unit
        override fun trackCategoryView(categoryKey: String, contentOrigin: String) = Unit
        override fun trackPlaceView(placeId: String, cityId: String?, zone: String?, placeCategory: String?, contentOrigin: String?) = Unit
        override fun trackActivityView(activityId: String, cityId: String?, zone: String?, contentOrigin: String?) = Unit
        override fun trackEventView(eventId: String, contentOrigin: String?) = Unit
        override fun trackInfoNoticeView(noticeId: String, cityId: String?, contentOrigin: String?) = Unit
        override fun trackNearbyView(zone: String?) = Unit
        override fun trackGuideOpen(activityId: String, cityId: String?) = Unit
        override fun trackAudioPlay(activityId: String, cityId: String?) = Unit
        override fun trackPremiumBundleOpen(contentOrigin: String?) = Unit
    }

    private class TestFavorites : FavoritesRepository {
        override val favoritesFlow: StateFlow<List<FavoriteEntry>> = MutableStateFlow(emptyList())
        override fun isFavorited(type: FavoriteItemType, id: String) = false
        override fun save(type: FavoriteItemType, id: String) = FavoriteEntry(type, id, 0)
        override fun remove(type: FavoriteItemType, id: String): FavoriteEntry? = null
        override fun restore(entry: FavoriteEntry) = Unit
        override fun getFavoriteKeySet(): Set<FavoriteKey> = emptySet()
    }

    private class TestLocation(val permission: Boolean = true, val fail: Boolean = false) : UserLocationProvider {
        var requests = 0
        override fun hasPermission() = permission
        override suspend fun getCurrentLocation(): GeoPoint? {
            requests++
            if (fail) error("GPS unavailable")
            return GeoPoint(42.4246, 18.7712)
        }
    }

    private class TestPlaces : PlaceRepository {
        var activeCalls = 0
        private val items = listOf(
            place("second", "old_town", 42.4260),
            place("nearest", "old_town", 42.4246),
            place("perast", "perast", 42.4860),
            place("restaurant", "old_town", 42.4246).copy(category = PlaceCategory.EAT_AND_DRINK),
            place("kotor_overview", "kotor", 42.4247).copy(subCategory = DESTINATION_OVERVIEW),
            place("perast_overview", "perast", 42.4861).copy(subCategory = DESTINATION_OVERVIEW)
        )
        override suspend fun getActivePlaces(): Resource<List<Place>, NetworkError, String?> { activeCalls++; return Resource.Success(items) }
        override suspend fun getMustSeePlaces() = Resource.Success(items.take(1))
        override suspend fun getPlacesByCategory(category: PlaceCategory) = Resource.Success(items.filter { it.category == category })
        override suspend fun getPlaceById(id: String) = Resource.Success(items.first { it.id == id })
    }

    private class TestEvents : EventRepository {
        var fail = true
        override suspend fun getUpcomingEvents(): Resource<List<Event>, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.UNKNOWN, "Offline") else Resource.Success(emptyList())
        override suspend fun getEvents(dateFilter: String, eventType: String) = getUpcomingEvents()
        override suspend fun getEventFilters() = Resource.Success(emptyList<EventFilter>())
        override suspend fun getEventById(id: String): Resource<Event, NetworkError, String?> = error("Unused")
    }

    private class TestPreferences : AppPreferences {
        private val values = mutableMapOf<String, String>()
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String) = values[key]
        override fun setString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
        override fun getSelectedLanguage() = "en"
        override fun setSelectedLanguage(language: String) { selectedLanguageFlow.value = language }
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
        override fun hasAudioPass() = false
        override fun setAudioPass(value: Boolean) = Unit
        override fun hasExplorerPass() = false
        override fun setExplorerPass(value: Boolean) = Unit
        override fun hasPerksPass() = false
        override fun setPerksPass(value: Boolean) = Unit
    }

    companion object {
        private fun place(id: String, zone: String, latitude: Double) = Place(
            id = id, cityId = "kotor", name = mapOf("en" to id), description = emptyMap(),
            longDescription = emptyMap(), localsTip = emptyMap(), category = PlaceCategory.SEE_AND_VISIT,
            subCategory = null, priceIndicator = null, latitude = latitude, longitude = 18.7712,
            zone = zone, tags = emptyList(), bestTime = BestTime.ANYTIME, estimatedDuration = null,
            openingHours = null, menuUrl = null, bookingPhone = null, photos = emptyList(), audioFile = null,
            audioAccessTier = "free", detailAccessTier = "free", isMustSee = false, isActive = true, sortOrder = 0
        )
    }
}
