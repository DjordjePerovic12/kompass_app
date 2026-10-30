package llc.bokadev.kompass.presentation.screens.listen

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText

data class ListenState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val view: ListenView = ListenView.ALL,
    val tracks: List<ListenTrack> = emptyList(),
    /** Some sources (places, activities or stories) couldn't load: the library may be incomplete. */
    val isIncomplete: Boolean = false
) : BaseViewState {
    val visible: List<ListenTrack>
        get() = if (view == ListenView.DOWNLOADED) tracks.filter { it.download == DownloadState.Downloaded } else tracks
}
