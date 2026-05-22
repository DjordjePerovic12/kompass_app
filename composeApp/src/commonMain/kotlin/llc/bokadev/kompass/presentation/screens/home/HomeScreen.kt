package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.permissions.LocationPermissionEffect
import llc.bokadev.kompass.presentation.screens.onboarding.OnboardingScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val HOME_ONBOARDING_SEEN_KEY = "home_onboarding_seen"
private const val HOME_ORIENTATION_SEEN_KEY = "home_orientation_seen"
private const val ONBOARDING_CONTEXT_KEY = "onboarding_context"

@Composable
fun HomeScreen(
    vmKey: String = "home",
    onNavigateToPlaceDetail: (String) -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {},
    onNavigateToInfoCenterDetail: (String) -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToItineraryDetail: (String) -> Unit = {},
    onNavigateToMustSeePlaces: () -> Unit = {},
    onNavigateToNearbyPlaces: () -> Unit = {},
    onNavigateToInfoCenter: () -> Unit = {},
    onNavigateToMyGuides: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToChangeLanguage: () -> Unit = {}
) {
    val vm: HomeViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val locationProvider = koinInject<UserLocationProvider>()
    val analytics = koinInject<AnalyticsRepository>()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val preferences = koinInject<AppPreferences>()
    val strings = rememberAppStrings()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()
    val onboardingContext = preferences.getString(ONBOARDING_CONTEXT_KEY) ?: "planning_ahead"
    var showOnboarding by remember {
        mutableStateOf(preferences.getString(HOME_ONBOARDING_SEEN_KEY) != "true")
    }
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var showLocationPrompt by remember {
        mutableStateOf(
            !locationProvider.hasPermission() &&
                !preferences.hasSeenLocationEducationPrompt()
        )
    }
    var showOrientationLayer by remember {
        mutableStateOf(!showOnboarding && preferences.getString(HOME_ORIENTATION_SEEN_KEY) != "true")
    }

    fun consumeOrientationLayer() {
        if (showOrientationLayer) {
            preferences.setString(HOME_ORIENTATION_SEEN_KEY, "true")
            showOrientationLayer = false
        }
    }

    LaunchedEffect(Unit) {
        if (!locationProvider.hasPermission() && preferences.hasSeenLocationEducationPrompt()) {
            shouldRequestLocation = true
        }
    }

    if (showLocationPrompt) {
        AlertDialog(
            onDismissRequest = { showLocationPrompt = false },
            title = { Text(strings.locationPromptTitle) },
            text = { Text(strings.locationPromptBody) },
            confirmButton = {
                TextButton(onClick = {
                    showLocationPrompt = false
                    preferences.setSeenLocationEducationPrompt(true)
                    shouldRequestLocation = true
                }) { Text(strings.continueLabel) }
            },
            dismissButton = {
                TextButton(onClick = {
                    preferences.setSeenLocationEducationPrompt(true)
                    showLocationPrompt = false
                }) { Text(strings.notNow) }
            }
        )
    }

    LocationPermissionEffect(
        enabled = shouldRequestLocation,
        onPermissionResult = { granted ->
            shouldRequestLocation = false
            if (granted && !state.isUsingCurrentLocation) {
                vm.onIntent(HomeEvent.LoadHomeData)
            }
        }
    )

    Box {
        HomeScreenContent(
            state = state,
            favoriteKeySet = favorites.map { FavoriteKey(it.type, it.id) }.toSet(),
            showOrientationLayer = showOrientationLayer,
            onboardingContext = onboardingContext,
            onPlaceClick = { id ->
                consumeOrientationLayer()
                state.mustSeePlaces.firstOrNull { it.id == id }?.let { place ->
                    analytics.trackPlaceView(
                        placeId = place.id,
                        cityId = place.cityId,
                        zone = place.zone,
                        placeCategory = place.category.name.lowercase(),
                        contentOrigin = "must_see"
                    )
                } ?: state.nearbyPlaces.firstOrNull { it.place.id == id }?.place?.let { place ->
                    analytics.trackPlaceView(
                        placeId = place.id,
                        cityId = place.cityId,
                        zone = place.zone,
                        placeCategory = place.category.name.lowercase(),
                        contentOrigin = "nearby"
                    )
                }
                onNavigateToPlaceDetail(id)
            },
            onEventClick = { id ->
                consumeOrientationLayer()
                analytics.trackEventView(id, contentOrigin = "events_home")
                onNavigateToEventDetail(id)
            },
            onNoticeClick = { id ->
                consumeOrientationLayer()
                state.infoCenterNotices.firstOrNull { it.id == id }?.let { notice ->
                    analytics.trackInfoNoticeView(
                        noticeId = notice.id,
                        cityId = notice.cityId,
                        contentOrigin = "news_home"
                    )
                }
                onNavigateToInfoCenterDetail(id)
            },
            onEventsSeeAll = {
                consumeOrientationLayer()
                onNavigateToEvents()
            },
            onMustSeeSeeAll = {
                consumeOrientationLayer()
                onNavigateToMustSeePlaces()
            },
            onNearbySeeAll = {
                consumeOrientationLayer()
                onNavigateToNearbyPlaces()
            },
            onInfoCenterSeeAll = {
                consumeOrientationLayer()
                onNavigateToInfoCenter()
            },
            onMyGuidesClick = {
                consumeOrientationLayer()
                onNavigateToMyGuides()
            },
            onSearchClick = {
                consumeOrientationLayer()
                onNavigateToSearch()
            },
            onChangeLanguageClick = {
                consumeOrientationLayer()
                onNavigateToChangeLanguage()
            },
            onPlaceFavoriteToggle = { id ->
                consumeOrientationLayer()
                favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, id)
            },
            onIntent = vm::onIntent
        )

        if (showOnboarding) {
            OnboardingScreen(
                initialContext = onboardingContext,
                onComplete = { selectedContext ->
                    preferences.setString(ONBOARDING_CONTEXT_KEY, selectedContext)
                    preferences.setString(HOME_ONBOARDING_SEEN_KEY, "true")
                    preferences.setString(HOME_ORIENTATION_SEEN_KEY, "false")
                    showOnboarding = false
                    showOrientationLayer = true
                }
            )
        }
    }
}
