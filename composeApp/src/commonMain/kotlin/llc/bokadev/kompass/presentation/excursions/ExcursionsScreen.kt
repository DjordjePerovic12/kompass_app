package llc.bokadev.kompass.presentation.excursions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect

@Composable
fun ExcursionsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExcursionsViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.excursionsState.collectAsStateWithLifecycle()

    StatusBarIconsEffect(lightIcons = true)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    val content: @Composable () -> Unit = {
        ExcursionsScreenContent(
            state = state,
            onBackClick = { viewModel.onEvent(ExcursionsEvent.NavigateBack) },
            onRetryClick = { viewModel.onEvent(ExcursionsEvent.OnRetryClick) },
            onFilterChange = { viewModel.onEvent(ExcursionsEvent.OnFilterChange(it)) },
            onSaveClick = { type, id, saved -> viewModel.onEvent(ExcursionsEvent.OnSaveClick(type, id, saved)) },
            onExcursionClick = { type, id -> viewModel.onEvent(ExcursionsEvent.OnExcursionClick(type, id)) }
        )
    }

    // The header, filters and back button stay on screen while loading or after a failure;
    // the content draws its inline spinner / error + retry itself, so both slots show it.
    BaseContentView(
        modifier = modifier,
        viewState = state,
        loading = content,
        error = { content() }
    ) {
        content()
    }
}
