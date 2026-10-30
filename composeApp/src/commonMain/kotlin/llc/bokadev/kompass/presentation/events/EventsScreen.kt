package llc.bokadev.kompass.presentation.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.eventsState.collectAsStateWithLifecycle()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    // Loading and failure render inline under the header with the filters still usable,
    // so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        EventsScreenContent(
            state = state,
            onBackClick = { viewModel.onEvent(EventsEvent.NavigateBack) },
            onRetryClick = { viewModel.onEvent(EventsEvent.OnRetryClick) },
            onDateFilterClick = { viewModel.onEvent(EventsEvent.OnDateFilterClick(it)) },
            onTypeFilterClick = { viewModel.onEvent(EventsEvent.OnTypeFilterClick(it)) },
            onResetFiltersClick = { viewModel.onEvent(EventsEvent.OnResetFiltersClick) },
            onEventClick = { viewModel.onEvent(EventsEvent.OnEventClick(it)) }
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
