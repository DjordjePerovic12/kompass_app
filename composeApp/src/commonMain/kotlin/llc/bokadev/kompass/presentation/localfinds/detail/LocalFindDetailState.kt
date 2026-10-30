package llc.bokadev.kompass.presentation.localfinds.detail

import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

data class NextStory(val id: String, val title: String)

/** [isLoading]: nothing to show yet. [error]: the story could not be loaded. */
data class LocalFindDetailState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val find: LocalFind? = null,
    /** The story's audio in the app language (or English fallback); null when it has none. */
    val audioTrack: AudioTrack? = null,
    /** The following story in the list order of the filter the story was opened from. */
    val next: NextStory? = null
) : BaseViewState(isLoading, error)
