package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent

/** One-off UI effects only — never navigation (that's [llc.bokadev.kompass.core.presentation.mvi.Navigator]'s job). */
sealed interface HomeEvent : BaseEvent {
    /** The user confirmed the location-education dialog; the platform permission prompt should fire now. */
    data object TriggerLocationPermissionRequest : HomeEvent
}
