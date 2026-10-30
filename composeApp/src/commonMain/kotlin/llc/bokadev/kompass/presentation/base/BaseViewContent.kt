package llc.bokadev.kompass.presentation.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.StateMessage
import org.jetbrains.compose.resources.stringResource
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
    A base wrapper composable for structuring screen content.

    This function provides a common layout structure for screens, handling:
    - System bar padding adjustments (optional). Kompass screens live inside MainShell's
      Scaffold, which already handles the system bars, so the default is `false`.
    - Displaying a top app bar and floating action button (FAB).
    - Showing a loading indicator while [viewState.isLoading] is `true` (nothing to show yet).
    - Showing [viewState.error] with a retry action when [onRetry] is given (nothing usable
      could be loaded). A refresh failure with content on screen is a screen-specific flag.
    - Otherwise injecting [content].

    @param modifier The [Modifier] to be applied to the layout.
    @param viewState The current state of the screen.
    @param onRetry Called by the default error view's retry action; `null` hides the error view.
    @param applySystemBarsPadding If `true`, applies padding for system bars.
    @param topBar A composable lambda function for defining the top app bar (default: empty).
    @param fab A composable lambda function for defining the floating action button (FAB) (default: empty).
    @param content The main content of the screen, provided as a lambda function with access to [viewState].
 **/

@Composable
fun BaseContentView(
    modifier: Modifier = Modifier,
    viewState: BaseViewState,
    onRetry: (() -> Unit)? = null,
    applySystemBarsPadding: Boolean = false,
    topBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (BaseViewState) -> Unit
) {

    val customModifier = remember(modifier, applySystemBarsPadding) {
        if (applySystemBarsPadding) {
            modifier
                .systemBarsPadding()
                .imePadding()
                .fillMaxSize()
        } else {
            modifier
                .imePadding()
                .fillMaxSize()
        }
    }

    Scaffold(
        modifier = customModifier,
        topBar = topBar,
        floatingActionButton = fab,
        containerColor = Color.Transparent,
        // Without system bar padding the screen handles insets itself; don't add them a second time.
        contentWindowInsets = if (applySystemBarsPadding) ScaffoldDefaults.contentWindowInsets else WindowInsets(0)
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val error = viewState.error
            when {
                viewState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        alignment = Alignment.Center
                    ),
                    color = KompassTheme.colors.lampYellow
                )

                error != null && onRetry != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StateMessage(
                        text = stringResource(error.messageRes),
                        actionLabel = rememberAppStrings().retry,
                        onAction = onRetry
                    )
                }

                else -> content(viewState)
            }
        }
    }
}
