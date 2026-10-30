package llc.bokadev.kompass.presentation.explore

import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** Explore's entry points. */
enum class ExploreDestination { PLACES, EAT_AND_DRINK, ACTIVITIES, WORTH_THE_DRIVE, STORIES, EVENTS, LOCAL_UPDATES, SERVICES }

/**
 * Explore is mostly static entry points; only these bits are dynamic, and each loads on its own.
 * A failed fetch leaves the default, so an entry point is never hidden just because loading
 * failed. Cards hide only when the data says their category is actually empty. So [isLoading]
 * stays false (the entry points show at once) and [error] is never set.
 */
data class ExploreState(
    override val isLoading: Boolean = false,
    override val error: BaseError? = null,
    /** CMS photo path for the Places hero: the first Must See place's first photo. */
    val heroPhoto: String? = null,
    val hasWorthTheDrive: Boolean = true,
    val worthTheDrivePhoto: String? = null,
    val hasStories: Boolean = true,
    val storiesHaveAudio: Boolean = false,
    /** ISO start of the next upcoming event, for the Events mini-calendar; null = generic icon. */
    val nextEventStart: String? = null,
    val hasImportantNotice: Boolean = false
) : BaseViewState(isLoading, error)
