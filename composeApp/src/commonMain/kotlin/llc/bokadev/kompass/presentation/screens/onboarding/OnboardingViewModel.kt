package llc.bokadev.kompass.presentation.screens.onboarding

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.util.AppPreferences

data class OnboardingState(
    /** False until the intro has been completed once on this device (existing preference key). */
    val completed: Boolean,
    override val isLoading: Boolean = false,
    override val error: UiText? = null
) : BaseViewState

sealed interface OnboardingAction : BaseAction {
    data object Start : OnboardingAction
}

/** The first-run intro gate. Keeps the existing keys ("home_onboarding_seen", "onboarding_context"). */
class OnboardingViewModel(
    private val preferences: AppPreferences
) : BaseViewModel<OnboardingState, OnboardingAction, Nothing>(
    OnboardingState(completed = preferences.getString(SEEN_KEY) == "true")
) {
    override fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Start -> {
                // The context value is kept as stored (default "planning_ahead"); the pilot no longer asks.
                preferences.setString(CONTEXT_KEY, preferences.getString(CONTEXT_KEY) ?: "planning_ahead")
                preferences.setString(SEEN_KEY, "true")
                updateState { it.copy(completed = true) }
            }
        }
    }

    private companion object {
        const val SEEN_KEY = "home_onboarding_seen"
        const val CONTEXT_KEY = "onboarding_context"
    }
}
