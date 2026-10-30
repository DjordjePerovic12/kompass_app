package llc.bokadev.kompass.presentation.excursions

import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class ExcursionsEvent : BaseEvent {
    data object NavigateBack : ExcursionsEvent()
    data object OnRetryClick : ExcursionsEvent()
    data class OnFilterChange(val filter: ExcursionFilter) : ExcursionsEvent()

    /** [saved] is the state the visitor asked for (not a blind flip). */
    data class OnSaveClick(val type: FavoriteItemType, val id: String, val saved: Boolean) : ExcursionsEvent()
    data class OnExcursionClick(val type: FavoriteItemType, val id: String) : ExcursionsEvent()
}
