package llc.bokadev.kompass.presentation.infocenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView

/** "Local updates": town notices, disruptions and practical updates. */
@Composable
fun InfoCenterScreen(
    modifier: Modifier = Modifier,
    viewModel: InfoCenterViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.infoCenterState.collectAsStateWithLifecycle()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    // Loading and failure render inline under the header, so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        InfoCenterScreenContent(
            state = state,
            onBackClick = { viewModel.onEvent(InfoCenterEvent.NavigateBack) },
            onRetryClick = { viewModel.onEvent(InfoCenterEvent.OnRetryClick) },
            onNoticeClick = { viewModel.onEvent(InfoCenterEvent.OnNoticeClick(it)) }
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
