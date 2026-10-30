package llc.bokadev.kompass.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView

/** Orchestrator for the first-run intro. MainShell owns the gate and reads `completed` itself. */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.onboardingState.collectAsStateWithLifecycle()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    BaseContentView(
        modifier = modifier,
        viewState = state
    ) {
        OnboardingScreenContent(
            onStartClick = { viewModel.onEvent(OnboardingEvent.OnStartClick) }
        )
    }
}
