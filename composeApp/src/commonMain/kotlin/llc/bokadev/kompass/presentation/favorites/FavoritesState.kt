package llc.bokadev.kompass.presentation.favorites

import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState
import llc.bokadev.kompass.presentation.shared.GROUP_OTHER

/**
 * [isLoading]: saved items are still being resolved (shown from their snapshots meanwhile).
 * Saved never fails as a whole, so [error] stays null: unresolved items show their snapshot and [refreshFailed].
 */
data class FavoritesState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val groups: List<SavedGroup> = emptyList(),
    val inRegion: Boolean = false,
    /** Some saved items couldn't be refreshed and are shown from what was remembered. */
    val refreshFailed: Boolean = false
) : BaseViewState(isLoading, error) {
    val savedCount: Int get() = groups.sumOf { it.items.size }
}

/** How far a saved entry could be resolved against current content. */
enum class SavedItemStatus {
    /** Found in the loaded content. */
    RESOLVED,
    /** Content is still loading; shown from its snapshot meanwhile. */
    LOADING,
    /** Content couldn't be fetched (offline/failure); shown from its snapshot, never called unavailable. */
    NOT_REFRESHED,
    /** The content list loaded fine and this id isn't in it any more. */
    UNAVAILABLE
}

/** One saved entry, resolved as far as the data allows. */
data class SavedItem(
    val entry: FavoriteEntry,
    val place: Place? = null,
    val activity: Experience? = null,
    /** Last-known name/photo, used when the item can't be resolved (offline or unpublished). */
    val snapshot: SavedSnapshot? = null,
    val status: SavedItemStatus = SavedItemStatus.RESOLVED,
    /** Null unless the user is in the bay. */
    val distanceKm: Double? = null,
    val groupKey: String = GROUP_OTHER
) {
    val unavailable: Boolean get() = status == SavedItemStatus.UNAVAILABLE
}

data class SavedGroup(val key: String, val items: List<SavedItem>)
