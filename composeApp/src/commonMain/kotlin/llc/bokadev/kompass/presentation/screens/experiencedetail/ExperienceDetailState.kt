package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Experience

data class ExperienceDetailState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val activity: Experience? = null,
    /** The activity's audio in the app language (or English fallback); null when it has none. */
    val audioTrack: AudioTrack? = null,
    val isSaved: Boolean = false
) : BaseViewState
