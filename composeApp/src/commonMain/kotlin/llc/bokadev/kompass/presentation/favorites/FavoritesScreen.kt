package llc.bokadev.kompass.presentation.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.saved_removed
import kompass.composeapp.generated.resources.saved_undo
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.presentation.SnackbarMessage
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

/** Saved orchestrator: ViewModel, state, and the "Removed · Undo" confirmation. */
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.favoritesState.collectAsStateWithLifecycle()
    // The confirmation carries an Undo action, which the String snackbar can't.
    val snackbarBus = koinInject<SnackbarBus>()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is FavoritesEvent.ShowRemoved) {
            snackbarBus.show(
                SnackbarMessage(
                    text = getString(Res.string.saved_removed, event.title),
                    actionLabel = getString(Res.string.saved_undo),
                    onAction = { viewModel.onEvent(FavoritesEvent.OnUndoClick(event.entry)) }
                )
            )
        }
    }

    // Saved keeps showing its items (from their snapshots) while they resolve, and never fails as a
    // whole, so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        FavoritesScreenContent(
            state = state,
            onLanguageClick = { viewModel.onEvent(FavoritesEvent.OnLanguageClick) },
            onRetryClick = { viewModel.onEvent(FavoritesEvent.OnRetryClick) },
            onExploreKotorClick = { viewModel.onEvent(FavoritesEvent.OnExploreKotorClick) },
            onItemClick = { type, id -> viewModel.onEvent(FavoritesEvent.OnItemClick(type, id)) },
            onRemoveClick = { entry, title -> viewModel.onEvent(FavoritesEvent.OnRemoveClick(entry, title)) }
        )
    }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state,
        loading = content,
        error = { content() }
    ) {
        content()
    }
}
