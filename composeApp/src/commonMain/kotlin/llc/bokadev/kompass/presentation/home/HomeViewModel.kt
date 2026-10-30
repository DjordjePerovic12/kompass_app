package llc.bokadev.kompass.presentation.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.HomeArea
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetDestinationOverviewUseCase
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetNearbyFallbackPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetNearbyPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetUpcomingEventsUseCase
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost

private const val NEARBY_RADIUS_KM = 3.5
private const val NEARBY_LIMIT = 4
private const val NOTICES_LIMIT = 4
private const val LOCATION_TIMEOUT_MS = 8_000L

class HomeViewModel(
    private val getMustSeePlaces: GetMustSeePlacesUseCase,
    private val getUpcomingEvents: GetUpcomingEventsUseCase,
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase,
    private val getNearbyPlaces: GetNearbyPlacesUseCase,
    private val getNearbyFallbackPlaces: GetNearbyFallbackPlacesUseCase,
    private val getDestinationOverview: GetDestinationOverviewUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val preferences: AppPreferences,
    private val analytics: AnalyticsRepository,
    private val favorites: FavoritesRepository,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost
) : BaseViewModel<HomeState, HomeEvent>() {

    override val initialViewState = HomeState(
        area = HomeArea.restore(preferences.getString(HomeArea.PREFERENCE_KEY))
    )
    private var hasLoadedInitialData = false
    private val jobs = mutableMapOf<HomeSection, Job>()

    init {
        viewModelScope.launch {
            favorites.favoritesFlow.collect { saved ->
                state.update { it.copy(favoriteKeys = saved.map { entry -> FavoriteKey(entry.type, entry.id) }.toSet()) }
            }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val homeState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                HomeSection.entries.forEach(::load)
                val promptAlreadySeen = preferences.hasSeenLocationEducationPrompt()
                if (!userLocationProvider.hasPermission() && !promptAlreadySeen) {
                    state.update { it.copy(showLocationPrompt = true) }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRefresh -> HomeSection.entries.forEach(::load)
            is HomeEvent.OnRetrySectionClick -> load(event.section)

            is HomeEvent.OnAreaSelect -> {
                preferences.setString(HomeArea.PREFERENCE_KEY, event.area.preferenceValue)
                state.update { it.copy(area = event.area, nearbyPlaces = emptyList(), areaOverview = null, isUsingCurrentLocation = false) }
                load(HomeSection.NEARBY)
                if (event.area == HomeArea.DEVICE && !userLocationProvider.hasPermission()) {
                    state.update { it.copy(showLocationPrompt = true) }
                }
            }

            is HomeEvent.OnPlaceClick -> {
                trackPlaceView(event.id, event.origin)
                navigate(Screen.PlaceDetailScreen(event.id))
            }

            is HomeEvent.OnListenClick -> {
                trackPlaceView(event.placeId, origin = "nearby_listen")
                // The current place detail plays the audio; the legacy guide screen is no longer used.
                navigate(Screen.PlaceDetailScreen(event.placeId, autoplay = true))
            }

            is HomeEvent.OnEventClick -> {
                analytics.trackEventView(event.id, contentOrigin = "events_home")
                navigate(Screen.EventDetailScreen(event.id))
            }

            is HomeEvent.OnNoticeClick -> {
                state.value.infoCenterNotices.firstOrNull { it.id == event.id }?.let {
                    analytics.trackInfoNoticeView(noticeId = it.id, cityId = it.cityId, contentOrigin = "news_home")
                }
                navigate(Screen.InfoCenterDetailScreen(event.id))
            }

            is HomeEvent.OnFavoriteChange ->
                if (event.saved) favorites.save(FavoriteItemType.PLACE, event.placeId)
                else favorites.remove(FavoriteItemType.PLACE, event.placeId)

            // The shared category list; a manual area carries over as its area filter.
            HomeEvent.OnSeeAllNearbyClick -> navigate(
                Screen.CategoryListScreen(
                    CategoryListKind.PLACES.name,
                    area = state.value.area.takeIf { it != HomeArea.DEVICE }?.preferenceValue
                )
            )

            HomeEvent.OnSeeAllMustSeeClick -> navigate(Screen.CategoryListScreen(CategoryListKind.MUST_SEE.name))
            HomeEvent.OnSeeAllInfoCenterClick -> navigate(Screen.InfoCenterScreen)
            HomeEvent.OnSeeAllEventsClick -> navigate(Screen.EventsScreen)
            HomeEvent.OnSearchClick -> navigate(Screen.SearchScreen)
            HomeEvent.OnLanguageClick -> languageSheet.open()
            HomeEvent.OnMyGuidesClick -> navigate(Screen.ListenScreen)

            HomeEvent.OnLocationPromptContinueClick -> {
                preferences.setSeenLocationEducationPrompt(true)
                state.update { it.copy(showLocationPrompt = false) }
                requestLocationPermission()
            }

            HomeEvent.OnLocationPromptDismiss -> {
                preferences.setSeenLocationEducationPrompt(true)
                state.update { it.copy(showLocationPrompt = false) }
            }

            is HomeEvent.OnLocationPermissionResult -> {
                if (event.granted && !state.value.isUsingCurrentLocation) load(HomeSection.NEARBY)
            }

            HomeEvent.TriggerLocationPermissionRequest -> Unit
        }
    }

    private fun requestLocationPermission() {
        viewModelScope.launch {
            event.emit(HomeEvent.TriggerLocationPermissionRequest)
        }
    }

    /** The destination is resolved now, from the state the visitor tapped on. */
    private fun navigate(destination: Screen) {
        viewModelScope.launch {
            navigator.navigateTo(destination)
        }
    }

    private fun trackPlaceView(placeId: String, origin: String) {
        val place = (state.value.mustSeePlaces + state.value.nearbyPlaces.map { it.place })
            .firstOrNull { it.id == placeId } ?: return
        analytics.trackPlaceView(
            placeId = place.id,
            cityId = place.cityId,
            zone = place.zone,
            placeCategory = place.category.name.lowercase(),
            contentOrigin = origin
        )
    }

    private fun load(section: HomeSection) {
        jobs[section]?.cancel()
        jobs[section] = viewModelScope.launch {
            state.update { it.withStatus(section, SectionStatus.LOADING) }
            try {
                val loaded = when (section) {
                    HomeSection.HIGHLIGHTS -> getMustSeePlaces()
                        .onSuccess { result -> state.update { it.copy(mustSeePlaces = result) } } is Resource.Success

                    HomeSection.EVENTS -> getUpcomingEvents()
                        .onSuccess { result -> state.update { it.copy(upcomingEvents = result) } } is Resource.Success

                    HomeSection.NOTICES -> getCurrentInfoNotices()
                        .onSuccess { result -> state.update { it.copy(infoCenterNotices = result.take(NOTICES_LIMIT)) } } is Resource.Success

                    HomeSection.NEARBY -> loadSuggestions()
                }
                state.update { it.withStatus(section, if (loaded) SectionStatus.READY else SectionStatus.FAILED) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state.update { it.withStatus(section, SectionStatus.FAILED) }
            }
        }
    }

    /** False when the suggestions could not be loaded. */
    private suspend fun loadSuggestions(): Boolean {
        val area = state.value.area
        val location = if (area == HomeArea.DEVICE && userLocationProvider.hasPermission()) {
            try {
                withTimeoutOrNull(LOCATION_TIMEOUT_MS) { userLocationProvider.getCurrentLocation() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        } else null

        val suggestions = (if (location != null) {
            getNearbyPlaces(
                origin = location,
                limit = NEARBY_LIMIT,
                category = PlaceCategory.SEE_AND_VISIT,
                maxDistanceKm = NEARBY_RADIUS_KM
            ).getOrNull()
        } else {
            val effectiveArea = if (area == HomeArea.DEVICE) HomeArea.OLD_TOWN else area
            getNearbyFallbackPlaces(
                category = PlaceCategory.SEE_AND_VISIT,
                matchesZone = effectiveArea::matchesZone,
                limit = NEARBY_LIMIT
            ).getOrNull()
        }) ?: return false
        // The area's editorial introduction; its absence or failure never fails the suggestions.
        val overviewArea = if (area == HomeArea.DEVICE) HomeArea.OLD_TOWN.takeIf { location == null } else area
        val overview = getDestinationOverview(
            matchesZone = overviewArea?.let { it::matchesZone },
            origin = location,
            maxDistanceKm = NEARBY_RADIUS_KM
        ).getOrNull()
        state.update { it.copy(nearbyPlaces = suggestions, areaOverview = overview, isUsingCurrentLocation = location != null) }
        return true
    }
}
