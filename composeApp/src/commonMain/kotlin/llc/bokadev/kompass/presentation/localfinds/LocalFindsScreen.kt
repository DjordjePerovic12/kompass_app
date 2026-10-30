package llc.bokadev.kompass.presentation.localfinds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect

/** Legends & Localisms orchestrator; the ViewModel opens stories and navigates back. */
@Composable
fun LocalFindsScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalFindsViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.localFindsState.collectAsStateWithLifecycle()

    StatusBarIconsEffect(lightIcons = false)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    // Loading and failure render inline under the header, so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        LocalFindsScreenContent(
            state = state,
            onBackClick = { viewModel.onEvent(LocalFindsEvent.NavigateBack) },
            onRetryClick = { viewModel.onEvent(LocalFindsEvent.OnRetryClick) },
            onFilterSelect = { viewModel.onEvent(LocalFindsEvent.OnFilterSelect(it)) },
            onStoryClick = { viewModel.onEvent(LocalFindsEvent.OnStoryClick(it)) }
        )
    }

    BaseContentView(
        modifier = modifier,
        viewState = state,
        loading = content,
        error = { content() }
    ) {
        content()
    }
}
