package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Place

data class PlaceDetailState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val place: Place? = null,
    /** The place's audio in the app language (or English fallback); null when it has none. */
    val audioTrack: AudioTrack? = null,
    val isSaved: Boolean = false
) : BaseViewState
