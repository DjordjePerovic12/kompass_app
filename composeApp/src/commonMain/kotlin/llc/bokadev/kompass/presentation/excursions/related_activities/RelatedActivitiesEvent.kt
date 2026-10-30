package llc.bokadev.kompass.presentation.excursions.related_activities

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class RelatedActivitiesEvent : BaseEvent {
    data object OnRetryClick : RelatedActivitiesEvent()

    /** [saved] is the state the visitor asked for (not a blind flip). */
    data class OnSaveClick(val activityId: String, val saved: Boolean) : RelatedActivitiesEvent()
    data class OnActivityClick(val activityId: String) : RelatedActivitiesEvent()
}
