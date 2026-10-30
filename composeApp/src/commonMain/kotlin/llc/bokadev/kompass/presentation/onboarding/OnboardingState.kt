package llc.bokadev.kompass.presentation.onboarding

import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** Nothing loads here: the gate reads a stored flag synchronously, so [isLoading] stays false. */
data class OnboardingState(
    /** False until the intro has been completed once on this device (existing preference key). */
    val completed: Boolean,
    override val isLoading: Boolean = false,
    override val error: BaseError? = null
) : BaseViewState(isLoading, error)
