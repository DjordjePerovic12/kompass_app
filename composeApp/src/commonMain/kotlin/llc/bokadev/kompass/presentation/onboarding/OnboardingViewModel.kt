package llc.bokadev.kompass.presentation.onboarding

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.presentation.base.BaseViewModel

/**
 * The first-run intro gate (not a route: MainShell shows it until [OnboardingState.completed]).
 * Keeps the existing keys ("home_onboarding_seen", "onboarding_context").
 */
class OnboardingViewModel(
    private val preferences: AppPreferences
) : BaseViewModel<OnboardingState, OnboardingEvent>() {

    override val initialViewState = OnboardingState(completed = preferences.getString(SEEN_KEY) == "true")

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    /** Nothing to load, so no onStart/stateIn: the gate must read the stored flag on the first frame. */
    val onboardingState: StateFlow<OnboardingState> = viewStateFlow

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnStartClick -> {
                // The context value is kept as stored (default "planning_ahead"); the pilot no longer asks.
                preferences.setString(CONTEXT_KEY, preferences.getString(CONTEXT_KEY) ?: "planning_ahead")
                preferences.setString(SEEN_KEY, "true")
                state.update { it.copy(completed = true) }
            }
        }
    }

    private companion object {
        const val SEEN_KEY = "home_onboarding_seen"
        const val CONTEXT_KEY = "onboarding_context"
    }
}
