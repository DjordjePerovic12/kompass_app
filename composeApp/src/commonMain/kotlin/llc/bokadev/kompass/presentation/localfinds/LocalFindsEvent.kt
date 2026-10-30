package llc.bokadev.kompass.presentation.localfinds

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind

sealed class LocalFindsEvent : BaseEvent {
    data object NavigateBack : LocalFindsEvent()
    data object OnRetryClick : LocalFindsEvent()

    /** [kind] null = All. */
    data class OnFilterSelect(val kind: StoryKind?) : LocalFindsEvent()

    /** Opens a story; the current filter goes along so "Next story" follows the same list. */
    data class OnStoryClick(val id: String) : LocalFindsEvent()
}
