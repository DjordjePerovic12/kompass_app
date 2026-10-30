package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText

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

data class LocalFindsState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    /** Null = All. */
    val filter: StoryKind? = null,
    /** Kinds that have at least one story; the segmented control shows only with two or more. */
    val kinds: List<StoryKind> = emptyList(),
    val cards: List<StoryCard> = emptyList()
) : BaseViewState
