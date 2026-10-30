package llc.bokadev.kompass.presentation.screens.categories

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText

/**
 * Explore is mostly static entry points; only these bits are dynamic, and each loads on its own.
 * A failed fetch leaves the default, so an entry point is never hidden just because loading
 * failed. Cards hide only when the data says their category is actually empty.
 */
data class CategoriesState(
    override val isLoading: Boolean = false,
    override val error: UiText? = null,
    /** CMS photo path for the Places hero: the first Must See place's first photo. */
    val heroPhoto: String? = null,
    val hasWorthTheDrive: Boolean = true,
    val worthTheDrivePhoto: String? = null,
    val hasStories: Boolean = true,
    val storiesHaveAudio: Boolean = false,
    /** ISO start of the next upcoming event, for the Events mini-calendar; null = generic icon. */
    val nextEventStart: String? = null,
    val hasImportantNotice: Boolean = false
) : BaseViewState
