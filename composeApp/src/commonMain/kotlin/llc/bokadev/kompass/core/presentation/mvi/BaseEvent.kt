package llc.bokadev.kompass.core.presentation.mvi

/**
 * Marker for a screen's sealed one-off UI-effect type (toasts, snackbars, external hand-offs) —
 * never navigation, that's [Navigator]'s job. Screens without one-off effects use `Nothing`.
 */
interface BaseEvent
