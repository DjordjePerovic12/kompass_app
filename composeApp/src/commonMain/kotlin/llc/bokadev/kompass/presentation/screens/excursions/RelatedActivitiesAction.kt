package llc.bokadev.kompass.presentation.screens.excursions

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface RelatedActivitiesAction : BaseAction {
    data class Load(val placeId: String, val force: Boolean = false) : RelatedActivitiesAction
    data class SetSaved(val activityId: String, val saved: Boolean) : RelatedActivitiesAction
    data class Open(val activityId: String) : RelatedActivitiesAction
}
