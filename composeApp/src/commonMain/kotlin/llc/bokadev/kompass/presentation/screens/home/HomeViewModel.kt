package llc.bokadev.kompass.presentation.screens.home

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetDestinationOverviewUseCase
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetNearbyFallbackPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetNearbyPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetUpcomingEventsUseCase
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import androidx.lifecycle.viewModelScope

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
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(
    initialState = HomeState(
        area = HomeArea.restore(preferences.getString(HomeArea.PREFERENCE_KEY)),
        showLocationPrompt = false
    )
) {
    private val jobs = mutableMapOf<HomeSection, Job>()

    override suspend fun onFirstStart() {
        HomeSection.entries.forEach(::load)
        val promptAlreadySeen = preferences.hasSeenLocationEducationPrompt()
        if (!userLocationProvider.hasPermission() && !promptAlreadySeen) {
            updateState { it.copy(showLocationPrompt = true) }
        }
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.LoadHomeData -> HomeSection.entries.forEach(::load)
            is HomeAction.RetrySection -> load(action.section)

            is HomeAction.SelectArea -> {
                preferences.setString(HomeArea.PREFERENCE_KEY, action.area.preferenceValue)
                updateState { it.copy(area = action.area, nearbyPlaces = emptyList(), areaOverview = null, isUsingCurrentLocation = false) }
                load(HomeSection.NEARBY)
                if (action.area == HomeArea.DEVICE && !userLocationProvider.hasPermission()) {
                    updateState { it.copy(showLocationPrompt = true) }
                }
            }

            is HomeAction.PlaceClicked -> {
                trackPlaceView(action.id, action.origin)
                navigator.navigateTo(Screen.PlaceDetailScreen(action.id))
            }

            is HomeAction.ListenClicked -> {
                trackPlaceView(action.placeId, origin = "nearby_listen")
                // The current place detail plays the audio; the legacy guide screen is no longer used.
                navigator.navigateTo(Screen.PlaceDetailScreen(action.placeId, autoplay = true))
            }

            is HomeAction.EventClicked -> {
                analytics.trackEventView(action.id, contentOrigin = "events_home")
                navigator.navigateTo(Screen.EventDetailScreen(action.id))
            }

            is HomeAction.NoticeClicked -> {
                state.value.infoCenterNotices.firstOrNull { it.id == action.id }?.let {
                    analytics.trackInfoNoticeView(noticeId = it.id, cityId = it.cityId, contentOrigin = "news_home")
                }
                navigator.navigateTo(Screen.InfoCenterDetailScreen(action.id))
            }

            is HomeAction.SetFavorite ->
                if (action.saved) favorites.save(FavoriteItemType.PLACE, action.placeId)
                else favorites.remove(FavoriteItemType.PLACE, action.placeId)

            // The shared category list; a manual area carries over as its area filter.
            HomeAction.SeeAllNearby -> navigator.navigateTo(
                Screen.CategoryListScreen(
                    CategoryListKind.PLACES.name,
                    area = state.value.area.takeIf { it != HomeArea.DEVICE }?.preferenceValue
                )
            )
            HomeAction.SeeAllMustSee -> navigator.navigateTo(Screen.CategoryListScreen(CategoryListKind.MUST_SEE.name))
            HomeAction.SeeAllInfoCenter -> navigator.navigateTo(Screen.InfoCenterScreen)
            HomeAction.SeeAllEvents -> navigator.navigateTo(Screen.EventsScreen)
            HomeAction.OpenSearch -> navigator.navigateTo(Screen.SearchScreen)
            HomeAction.OpenLanguagePicker -> languageSheet.open()
            HomeAction.OpenMyGuides -> navigator.navigateTo(Screen.ListenScreen)

            HomeAction.RequestLocationPermission -> {
                preferences.setSeenLocationEducationPrompt(true)
                updateState { it.copy(showLocationPrompt = false) }
                sendEvent(HomeEvent.TriggerLocationPermissionRequest)
            }

            HomeAction.DismissLocationPrompt -> {
                preferences.setSeenLocationEducationPrompt(true)
                updateState { it.copy(showLocationPrompt = false) }
            }

            is HomeAction.LocationPermissionResult -> {
                if (action.granted && !state.value.isUsingCurrentLocation) load(HomeSection.NEARBY)
            }
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
            updateState { it.withStatus(section, SectionStatus.LOADING) }
            try {
                val loaded = when (section) {
                    HomeSection.HIGHLIGHTS -> getMustSeePlaces()
                        .onSuccess { result -> updateState { it.copy(mustSeePlaces = result) } } is Resource.Success
                    HomeSection.EVENTS -> getUpcomingEvents()
                        .onSuccess { result -> updateState { it.copy(upcomingEvents = result) } } is Resource.Success
                    HomeSection.NOTICES -> getCurrentInfoNotices()
                        .onSuccess { result -> updateState { it.copy(infoCenterNotices = result.take(NOTICES_LIMIT)) } } is Resource.Success
                    HomeSection.NEARBY -> loadSuggestions()
                }
                updateState { it.withStatus(section, if (loaded) SectionStatus.READY else SectionStatus.FAILED) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.withStatus(section, SectionStatus.FAILED) }
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
        updateState { it.copy(nearbyPlaces = suggestions, areaOverview = overview, isUsingCurrentLocation = location != null) }
        return true
    }
}
