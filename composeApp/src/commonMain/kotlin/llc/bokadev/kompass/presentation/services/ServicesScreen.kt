package llc.bokadev.kompass.presentation.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView

@Composable
fun ServicesScreen(
    modifier: Modifier = Modifier,
    viewModel: ServicesViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.servicesState.collectAsStateWithLifecycle()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    // Loading and failure render inline under the header, so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        ServicesScreenContent(
            state = state,
            onBackClick = { viewModel.onEvent(ServicesEvent.NavigateBack) },
            onRetryClick = { viewModel.onEvent(ServicesEvent.OnRetryClick) },
            onServiceClick = { viewModel.onEvent(ServicesEvent.OnServiceClick(it)) }
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
