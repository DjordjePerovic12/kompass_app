package llc.bokadev.kompass.core.presentation.mvi

/**
 * A screen's state contract. [isLoading] means "nothing to show yet" (a refresh with content on
 * screen is a screen-specific flag), and [error] means "nothing usable could be loaded" — a
 * localized [UiText], never exception text.
 */
interface BaseViewState {
    val isLoading: Boolean
    val error: UiText?
}
