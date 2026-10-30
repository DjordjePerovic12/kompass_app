package llc.bokadev.kompass.presentation.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView

/** Orchestrator: entry points navigate from the ViewModel (no callbacks here). */
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.exploreState.collectAsStateWithLifecycle()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    // The entry points are static, so the state never loads or fails as a whole.
    BaseContentView(
        modifier = modifier,
        viewState = state
    ) {
        ExploreScreenContent(
            state = state,
            onDestinationClick = { viewModel.onEvent(ExploreEvent.OnDestinationClick(it)) },
            onSearchClick = { viewModel.onEvent(ExploreEvent.OnSearchClick) },
            onLanguageClick = { viewModel.onEvent(ExploreEvent.OnLanguageClick) }
        )
    }
}
