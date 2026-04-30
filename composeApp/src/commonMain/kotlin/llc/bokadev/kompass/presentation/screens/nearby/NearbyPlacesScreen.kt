package llc.bokadev.kompass.presentation.screens.nearby

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
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NearbyPlacesScreen(
    onBack: () -> Unit,
    onPlaceClick: (String) -> Unit
) {
    val vm: NearbyPlacesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val locationProvider = koinInject<UserLocationProvider>()
    var shouldRequestLocation by remember { mutableStateOf(false) }
    var showLocationPrompt by remember { mutableStateOf(!locationProvider.hasPermission()) }

    if (showLocationPrompt) {
        AlertDialog(
            onDismissRequest = { showLocationPrompt = false },
            title = { Text("Use your location for better nearby results") },
            text = { Text("Allow location to sort places by what is actually closest to you, including walkable spots and quick rides.") },
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
                vm.onIntent(NearbyPlacesEvent.LoadNearbyPlaces)
            }
        }
    )

    NearbyPlacesScreenContent(
        state = state,
        onIntent = vm::onIntent,
        onPlaceClick = onPlaceClick,
        topBar = {
            KompassSharedTopBar(
                slug = "Closest to you",
                title = "Nearby",
                onBackClick = onBack
            )
        }
    )
}
