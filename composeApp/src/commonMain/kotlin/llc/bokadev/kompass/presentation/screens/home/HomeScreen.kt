package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.permissions.LocationPermissionEffect
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Orchestrator only: resolve the ViewModel, collect state, drain one-off events, delegate
 * everything else to [HomeScreenContent]. Navigation is handled entirely by [HomeViewModel]
 * through its injected `Navigator` — this composable takes no navigation callbacks.
 */
@Composable
fun HomeScreen(vmKey: String = "home") {
    val vm: HomeViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()
    var shouldRequestLocationPermission by remember { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                HomeEvent.TriggerLocationPermissionRequest -> shouldRequestLocationPermission = true
            }
        }
    }

    LocationPermissionEffect(
        enabled = shouldRequestLocationPermission,
        onPermissionResult = { granted ->
            shouldRequestLocationPermission = false
            vm.onAction(HomeAction.LocationPermissionResult(granted))
        }
    )

    HomeScreenContent(
        state = state,
        favoriteKeySet = favorites.map { FavoriteKey(it.type, it.id) }.toSet(),
        onAction = vm::onAction
    )
}
