package llc.bokadev.kompass.presentation.home

import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.core.utils.*
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.presentation.shared.dotWindow

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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

    @Test fun failedEventsDoNotSuppressNearbyAndRetryIsIndependent() = screen { vm, _, data, events ->
        advanceUntilIdle()
        assertEquals(SectionStatus.FAILED, vm.homeState.value.sections[HomeSection.EVENTS])
        assertEquals(SectionStatus.READY, vm.homeState.value.sections[HomeSection.NEARBY])
        assertEquals(listOf("nearest", "second"), vm.homeState.value.nearbyPlaces.map { it.place.id })
        assertTrue(vm.homeState.value.isUsingCurrentLocation)
        val placeCalls = data.activeCalls
        events.fail = false
        vm.onEvent(HomeEvent.OnRetrySectionClick(HomeSection.EVENTS))
        advanceUntilIdle()
        assertEquals(SectionStatus.READY, vm.homeState.value.sections[HomeSection.EVENTS])
        assertEquals(placeCalls, data.activeCalls)
    }

    @Test fun manualAreaPersistsAndDoesNotAskForLocation() {
        val prefs = TestPreferences().apply { setString(HomeArea.PREFERENCE_KEY, "perast") }
        val location = TestLocation()
        screen(prefs = prefs, location = location) { vm, _, _, _ ->
            advanceUntilIdle()
            assertEquals(listOf("perast"), vm.homeState.value.nearbyPlaces.map { it.place.id })
            assertEquals(0, location.requests)
            assertFalse(vm.homeState.value.isUsingCurrentLocation)
            vm.onEvent(HomeEvent.OnAreaSelect(HomeArea.DOBROTA))
            advanceUntilIdle()
            assertEquals("dobrota", prefs.getString(HomeArea.PREFERENCE_KEY))
            assertEquals(HomeArea.DOBROTA, vm.homeState.value.effectiveArea)
            assertTrue(vm.homeState.value.nearbyPlaces.isEmpty())
            vm.onEvent(HomeEvent.OnAreaSelect(HomeArea.DEVICE))
            advanceUntilIdle()
            assertEquals(1, location.requests)
            assertTrue(vm.homeState.value.isUsingCurrentLocation)
        }
    }

    @Test fun deniedLocationUsesLabeledOldTownWithoutRequestingPermission() {
        val location = TestLocation(permission = false)
        screen(location = location) { vm, _, _, _ ->
            advanceUntilIdle()
            assertEquals(0, location.requests)
            assertEquals(HomeArea.DEVICE, vm.homeState.value.area)
            assertEquals(HomeArea.OLD_TOWN, vm.homeState.value.effectiveArea)
            assertFalse(vm.homeState.value.isUsingCurrentLocation)
            assertEquals(listOf("second", "nearest"), vm.homeState.value.nearbyPlaces.map { it.place.id })
        }
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

    @Test fun unavailableLocationFallsBackButDoesNotChangePreference() = screen(location = TestLocation(fail = true)) { vm, _, _, _ ->
        advanceUntilIdle()
        assertEquals(SectionStatus.READY, vm.homeState.value.sections[HomeSection.NEARBY])
        assertEquals(HomeArea.OLD_TOWN, vm.homeState.value.effectiveArea)
        assertEquals(HomeArea.DEVICE, vm.homeState.value.area)
    }

    @Test fun continuingTheLocationPromptAsksForPermissionAndHidesIt() {
        screen(location = TestLocation(permission = false)) { vm, _, _, _ ->
            // Choosing "my location" without permission brings the prompt up.
            vm.onEvent(HomeEvent.OnAreaSelect(HomeArea.DEVICE))
            advanceUntilIdle()
            assertTrue(vm.homeState.value.showLocationPrompt)
            val events = mutableListOf<HomeEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.toList(events) }
            vm.onEvent(HomeEvent.OnLocationPromptContinueClick)
            advanceUntilIdle()
            assertEquals(listOf<HomeEvent>(HomeEvent.TriggerLocationPermissionRequest), events)
            assertFalse(vm.homeState.value.showLocationPrompt)
        }
    }

    @Test fun savedPlacesComeFromTheViewModelState() {
        val favorites = TestFavorites()
        screen(favorites = favorites) { vm, _, _, _ ->
            advanceUntilIdle()
            assertTrue(vm.homeState.value.favoriteKeys.isEmpty())
            favorites.flow.value = listOf(FavoriteEntry(FavoriteItemType.PLACE, "nearest", 0))
            advanceUntilIdle()
            assertEquals(setOf(FavoriteKey(FavoriteItemType.PLACE, "nearest")), vm.homeState.value.favoriteKeys)
        }
    }

    @Test fun listenOpensPlaceDetailWithAutoplay() = screen { vm, navigator, _, _ ->
        advanceUntilIdle()
        vm.onEvent(HomeEvent.OnListenClick("nearest"))
        advanceUntilIdle()
        val command = navigator.recorded.first()
        assertEquals(Screen.PlaceDetailScreen("nearest", autoplay = true), (command as NavType.NavigateToRoute).destination)
    }

    @Test fun destinationOverviewsAreOfferedEditoriallyNotAsNearbyAttractions() = screen { vm, _, _, _ ->
        advanceUntilIdle()
        assertTrue(vm.homeState.value.nearbyPlaces.none { it.place.isDestinationOverview })
        assertEquals("kotor_overview", vm.homeState.value.areaOverview?.id)
        vm.onEvent(HomeEvent.OnAreaSelect(HomeArea.PERAST))
        advanceUntilIdle()
        assertEquals(listOf("perast"), vm.homeState.value.nearbyPlaces.map { it.place.id })
        assertEquals("perast_overview", vm.homeState.value.areaOverview?.id)
        vm.onEvent(HomeEvent.OnAreaSelect(HomeArea.DOBROTA))
        advanceUntilIdle()
        assertNull(vm.homeState.value.areaOverview, "no overview in the CMS → none shown, never a substitute")
    }

    @Test fun seeAllOpensTheSharedListWithTheManualArea() {
        val prefs = TestPreferences().apply { setString(HomeArea.PREFERENCE_KEY, "perast") }
        screen(prefs = prefs) { vm, navigator, _, _ ->
            advanceUntilIdle()
            vm.onEvent(HomeEvent.OnSeeAllNearbyClick)
            vm.onEvent(HomeEvent.OnAreaSelect(HomeArea.DEVICE))
            vm.onEvent(HomeEvent.OnSeeAllNearbyClick)
            vm.onEvent(HomeEvent.OnSeeAllMustSeeClick)
            advanceUntilIdle()
            val routes = navigator.recorded.take(3).map { (it as NavType.NavigateToRoute).destination }
            assertEquals(
                listOf(
                    Screen.CategoryListScreen("PLACES", area = "perast"),
                    Screen.CategoryListScreen("PLACES", area = null),
                    Screen.CategoryListScreen("MUST_SEE")
                ),
                routes
            )
        }
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

    /** Builds the ViewModel and collects its state like the screen does, which starts the initial load. */
    private fun screen(
        places: TestPlaces = TestPlaces(),
        events: TestEvents = TestEvents(),
        prefs: TestPreferences = TestPreferences(),
        location: TestLocation = TestLocation(),
        favorites: TestFavorites = TestFavorites(),
        block: suspend TestScope.(HomeViewModel, FakeNavigator, TestPlaces, TestEvents) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val navigator = FakeNavigator()
        val vm = model(places, events, prefs, location, navigator, favorites)
        backgroundScope.launch { vm.homeState.collect {} }
        try {
            block(vm, navigator, places, events)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private fun model(
        places: TestPlaces,
        events: TestEvents,
        prefs: TestPreferences,
        location: TestLocation,
        navigator: FakeNavigator,
        favorites: TestFavorites = TestFavorites()
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
            favorites = favorites,
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
        val flow = MutableStateFlow<List<FavoriteEntry>>(emptyList())
        override val favoritesFlow: StateFlow<List<FavoriteEntry>> = flow
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
