package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase

data class EssentialsState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val selectedTab: EssentialsTab = EssentialsTab.INFO,
    val groupedEssentials: Map<EssentialCategory, List<CityEssential>> = emptyMap(),
    val expandedIds: Set<String> = emptySet(),
    val sections: List<UtilityCategorySection> = emptyList(),
    val currentLocation: GeoPoint? = null
) : BaseState()

enum class EssentialsTab {
    INFO,
    UTILITY
}

sealed interface EssentialsEvent : BaseEvent {
    data class SelectTab(val tab: EssentialsTab) : EssentialsEvent
    data class ToggleItem(val id: String) : EssentialsEvent
    data object Retry : EssentialsEvent
}

class EssentialsViewModel(
    private val getEssentials: GetEssentialsUseCase,
    private val getUtilities: GetUtilitiesUseCase,
    private val userLocationProvider: UserLocationProvider
) : BaseViewModel<EssentialsState, EssentialsEvent>() {

    override val initialState = EssentialsState()

    init {
        load()
    }

    override fun onIntent(event: EssentialsEvent) {
        when (event) {
            is EssentialsEvent.SelectTab -> _state.update { it.copy(selectedTab = event.tab) }
            is EssentialsEvent.ToggleItem -> toggleItem(event.id)
            EssentialsEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
            val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

            val essentialsResult = getEssentials()
            val utilitiesResult = getUtilities()

            val groupedEssentials = essentialsResult.getOrDefault(emptyList()).groupBy { it.category }
            val sections = utilitiesResult.getOrDefault(emptyList())
                .filter { it.latitude != null && it.longitude != null }
                .groupBy { it.category }
                .mapNotNull { (category, items) ->
                    buildUtilitySection(category, items, origin, currentLocation)
                }
                .sortedBy { it.category.orderIndex() }

            val error = utilitiesResult.exceptionOrNull()?.message
                ?: essentialsResult.exceptionOrNull()?.message

            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (groupedEssentials.isEmpty() && sections.isEmpty()) error else null,
                    groupedEssentials = groupedEssentials,
                    sections = sections,
                    currentLocation = currentLocation
                )
            }
        }
    }

    private fun toggleItem(id: String) {
        _state.update {
            val newExpanded = if (id in it.expandedIds) it.expandedIds - id else it.expandedIds + id
            it.copy(expandedIds = newExpanded)
        }
    }
}
