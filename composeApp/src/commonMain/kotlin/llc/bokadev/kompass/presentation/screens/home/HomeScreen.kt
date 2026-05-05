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
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.permissions.LocationPermissionEffect
import org.koin.compose.koinInject
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
    onNavigateToNearbyPlaces: () -> Unit = {},
    onNavigateToInfoCenter: () -> Unit = {},
    onNavigateToMyGuides: () -> Unit = {}
) {
    val vm: HomeViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val locationProvider = koinInject<UserLocationProvider>()
    val analytics = koinInject<AnalyticsRepository>()
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var showLocationPrompt by remember { mutableStateOf(!locationProvider.hasPermission()) }

    LaunchedEffect(Unit) {
        analytics.trackScreenView("home")
    }

    if (showLocationPrompt) {
        AlertDialog(
            onDismissRequest = { showLocationPrompt = false },
            title = { Text("See what’s closest right now") },
            text = { Text("Allow location to show walkable places nearby and better local recommendations while you explore Kotor.") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationPrompt = false
                    shouldRequestLocation = true
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPrompt = false }) { Text("Not now") }
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
        onNearbySeeAll = onNavigateToNearbyPlaces,
        onInfoCenterSeeAll = onNavigateToInfoCenter,
        onMyGuidesClick = onNavigateToMyGuides,
        onIntent = vm::onIntent
    )
}
