package llc.bokadev.kompass.presentation.localfinds

import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState
import llc.bokadev.kompass.presentation.localfinds.utils.StoryCardLayout
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind

/** One list card, already resolved for the app language (English fallback). */
data class StoryCard(
    val id: String,
    val kind: StoryKind,
    val layout: StoryCardLayout,
    val title: String,
    val excerpt: String?,
    val area: String?,
    val photo: String?,
    val readMinutes: Int?,
    val hasAudio: Boolean
)

/** [isLoading]: nothing to show yet. [error]: no stories could be loaded. */
data class LocalFindsState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    /** Null = All. */
    val filter: StoryKind? = null,
    /** Kinds that have at least one story; the segmented control shows only with two or more. */
    val kinds: List<StoryKind> = emptyList(),
    val cards: List<StoryCard> = emptyList()
) : BaseViewState(isLoading, error)
