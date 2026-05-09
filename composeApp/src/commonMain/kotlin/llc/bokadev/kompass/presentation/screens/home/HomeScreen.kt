package llc.bokadev.kompass.presentation.screens.home

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
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.permissions.LocationPermissionEffect
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
    onNavigateToChangeLanguage: () -> Unit = {}
) {
    val vm: HomeViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val locationProvider = koinInject<UserLocationProvider>()
    val analytics = koinInject<AnalyticsRepository>()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val preferences = koinInject<AppPreferences>()
    val strings = rememberAppStrings()
    val favoriteEntries by favoritesRepository.favoritesFlow.collectAsState()
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var showLocationPrompt by remember {
        mutableStateOf(
            !locationProvider.hasPermission() &&
                !preferences.hasSeenLocationEducationPrompt()
        )
    }

    LaunchedEffect(Unit) {
        analytics.trackScreenView("home")
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

    HomeScreenContent(
        state = state,
        favoriteKeySet = favoritesRepository.getFavoriteKeySet(),
        onPlaceClick = { id ->
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
            analytics.trackEventView(id, contentOrigin = "events_home")
            onNavigateToEventDetail(id)
        },
        onNoticeClick = { id ->
            state.infoCenterNotices.firstOrNull { it.id == id }?.let { notice ->
                analytics.trackInfoNoticeView(
                    noticeId = notice.id,
                    cityId = notice.cityId,
                    contentOrigin = "news_home"
                )
            }
            onNavigateToInfoCenterDetail(id)
        },
        onEventsSeeAll = onNavigateToEvents,
        onMustSeeSeeAll = onNavigateToMustSeePlaces,
        onNearbySeeAll = onNavigateToNearbyPlaces,
        onInfoCenterSeeAll = onNavigateToInfoCenter,
        onMyGuidesClick = onNavigateToMyGuides,
        onChangeLanguageClick = onNavigateToChangeLanguage,
        onPlaceFavoriteToggle = { id ->
            favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, id)
        },
        onIntent = vm::onIntent
    )
}
