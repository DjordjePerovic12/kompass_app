package llc.bokadev.kompass.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.permissions.LocationPermissionEffect

/**
 * Orchestrator: collects state, owns the platform location-permission prompt, and
 * delegates everything else to [HomeScreenContent]. Navigation is the ViewModel's job.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    var shouldRequestLocationPermission by remember { mutableStateOf(false) }

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is HomeEvent.TriggerLocationPermissionRequest) {
            shouldRequestLocationPermission = true
        }
    }

    LocationPermissionEffect(
        enabled = shouldRequestLocationPermission,
        onPermissionResult = { granted ->
            shouldRequestLocationPermission = false
            viewModel.onEvent(HomeEvent.OnLocationPermissionResult(granted))
        }
    )

    // Sections load and fail on their own, inline, so the state never loads or fails as a whole.
    BaseContentView(
        modifier = modifier,
        viewState = state
    ) {
        HomeScreenContent(
            state = state,
            favoriteKeySet = state.favoriteKeys,
            onPlaceClick = { id, origin -> viewModel.onEvent(HomeEvent.OnPlaceClick(id, origin)) },
            onFavoriteChange = { id, saved -> viewModel.onEvent(HomeEvent.OnFavoriteChange(id, saved)) },
            onListenClick = { viewModel.onEvent(HomeEvent.OnListenClick(it)) },
            onEventClick = { viewModel.onEvent(HomeEvent.OnEventClick(it)) },
            onNoticeClick = { viewModel.onEvent(HomeEvent.OnNoticeClick(it)) },
            onAreaSelect = { viewModel.onEvent(HomeEvent.OnAreaSelect(it)) },
            onRetrySectionClick = { viewModel.onEvent(HomeEvent.OnRetrySectionClick(it)) },
            onSeeAllNearbyClick = { viewModel.onEvent(HomeEvent.OnSeeAllNearbyClick) },
            onSeeAllInfoCenterClick = { viewModel.onEvent(HomeEvent.OnSeeAllInfoCenterClick) },
            onSeeAllEventsClick = { viewModel.onEvent(HomeEvent.OnSeeAllEventsClick) },
            onSearchClick = { viewModel.onEvent(HomeEvent.OnSearchClick) },
            onLanguageClick = { viewModel.onEvent(HomeEvent.OnLanguageClick) },
            onLocationPromptContinueClick = { viewModel.onEvent(HomeEvent.OnLocationPromptContinueClick) },
            onLocationPromptDismiss = { viewModel.onEvent(HomeEvent.OnLocationPromptDismiss) }
        )
    }
}
