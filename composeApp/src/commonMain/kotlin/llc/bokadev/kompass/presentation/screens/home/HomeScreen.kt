package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.permissions.LocationPermissionEffect
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    vmKey: String = "home",
    onNavigateToPlaceDetail: (String) -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {},
    onNavigateToItineraryDetail: (String) -> Unit = {},
    onNavigateToNearbyPlaces: () -> Unit = {}
) {
    val vm: HomeViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val locationProvider = koinInject<UserLocationProvider>()
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var showLocationPrompt by remember { mutableStateOf(!locationProvider.hasPermission()) }

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
        onPlaceClick = onNavigateToPlaceDetail,
        onEventClick = onNavigateToEventDetail,
        onNearbySeeAll = onNavigateToNearbyPlaces,
        onIntent = vm::onIntent
    )
}
