package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase

data class UtilityMapState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val category: UtilityCategory? = null,
    val currentLocation: GeoPoint? = null,
    val items: List<UtilityLocationItem> = emptyList(),
    val selectedUtilityId: String? = null
) : BaseState()

sealed interface UtilityMapEvent : BaseEvent {
    data class SelectUtility(val id: String) : UtilityMapEvent
    data object Retry : UtilityMapEvent
}

class UtilityMapViewModel(
    savedStateHandle: SavedStateHandle,
    private val getUtilities: GetUtilitiesUseCase,
    private val userLocationProvider: UserLocationProvider
) : BaseViewModel<UtilityMapState, UtilityMapEvent>() {

    private var cachedUtilities: List<Utility> = emptyList()

    override val initialState = UtilityMapState(
        category = savedStateHandle.get<String>("category")
            ?.let { runCatching { UtilityCategory.valueOf(it) }.getOrNull() }
    )

    init {
        load()
        startLocationRefresh()
    }

    override fun onIntent(event: UtilityMapEvent) {
        when (event) {
            is UtilityMapEvent.SelectUtility -> _state.update { it.copy(selectedUtilityId = event.id) }
            UtilityMapEvent.Retry -> load()
        }
    }

    private fun load() {
        val category = state.value.category ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
            val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

            val utilitiesResult = getUtilities()
            cachedUtilities = utilitiesResult.getOrDefault(emptyList())
            val items = buildUtilityLocationItems(category, cachedUtilities, origin)

            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (items.isEmpty()) utilitiesResult.exceptionOrNull()?.message else null,
                    currentLocation = currentLocation,
                    items = items,
                    selectedUtilityId = items.firstOrNull()?.utility?.id
                )
            }
        }
    }

    private fun startLocationRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5_000L)
                refreshLocationContext()
            }
        }
    }

    private suspend fun refreshLocationContext() {
        val category = state.value.category ?: return

        if (!userLocationProvider.hasPermission()) {
            _state.update { current ->
                if (current.currentLocation == null) current else current.copy(currentLocation = null)
            }
            return
        }

        val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
        val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER
        val items = buildUtilityLocationItems(category, cachedUtilities, origin)

        _state.update { current ->
            val selectedId = current.selectedUtilityId
                ?.takeIf { id -> items.any { it.utility.id == id } }
                ?: items.firstOrNull()?.utility?.id

            current.copy(
                currentLocation = currentLocation,
                items = items,
                selectedUtilityId = selectedId
            )
        }
    }
}
