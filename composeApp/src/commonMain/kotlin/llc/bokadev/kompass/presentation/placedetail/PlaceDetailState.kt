package llc.bokadev.kompass.presentation.placedetail

import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** [isLoading]: nothing to show yet. [error]: nothing usable could be loaded. */
data class PlaceDetailState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val place: Place? = null,
    /** The place's audio in the app language (or English fallback); null when it has none. */
    val audioTrack: AudioTrack? = null,
    val isSaved: Boolean = false
) : BaseViewState(isLoading, error)
