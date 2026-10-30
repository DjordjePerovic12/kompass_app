package llc.bokadev.kompass.presentation.screens.favorites

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText

data class FavoritesState(
    override val isLoading: Boolean = true,
    /** Saved never fails as a whole: unresolved items show their snapshot and [refreshFailed]. */
    override val error: UiText? = null,
    val groups: List<SavedGroup> = emptyList(),
    val inRegion: Boolean = false,
    /** Some saved items couldn't be refreshed and are shown from what was remembered. */
    val refreshFailed: Boolean = false
) : BaseViewState {
    val savedCount: Int get() = groups.sumOf { it.items.size }
}
