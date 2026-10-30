package llc.bokadev.kompass.presentation.onboarding

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class OnboardingEvent : BaseEvent {
    data object OnStartClick : OnboardingEvent()
}
