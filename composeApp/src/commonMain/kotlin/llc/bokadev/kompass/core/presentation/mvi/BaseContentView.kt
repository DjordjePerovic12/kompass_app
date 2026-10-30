package llc.bokadev.kompass.core.presentation.mvi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * For read screens whose whole content loads or fails as one: shows [loading] while
 * [BaseViewState.isLoading], [error] when [BaseViewState.error] is set, otherwise [content].
 * A refresh with content already on screen must not set `isLoading` (keep the content visible
 * and show a screen-specific inline status instead).
 */
@Composable
fun BaseContentView(
    state: BaseViewState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { DefaultLoading() },
    error: @Composable (message: String) -> Unit = { DefaultError(it, onRetry) },
    content: @Composable () -> Unit
) {
    Box(modifier.fillMaxSize()) {
        val failure = state.error
        when {
            state.isLoading -> loading()
            failure != null -> error(failure.asString())
            else -> content()
        }
    }
}

@Composable
private fun DefaultLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = KompassTheme.colors.lampYellow)
    }
}

@Composable
private fun DefaultError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        StateMessage(text = message, actionLabel = rememberAppStrings().retry, onAction = onRetry)
    }
}
