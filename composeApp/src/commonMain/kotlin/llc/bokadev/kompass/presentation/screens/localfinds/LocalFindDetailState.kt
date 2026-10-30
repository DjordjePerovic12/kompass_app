package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.LocalFind

data class NextStory(val id: String, val title: String)

data class LocalFindDetailState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val find: LocalFind? = null,
    /** The story's audio in the app language (or English fallback); null when it has none. */
    val audioTrack: AudioTrack? = null,
    /** The following story in the list order of the filter the story was opened from. */
    val next: NextStory? = null
) : BaseViewState
