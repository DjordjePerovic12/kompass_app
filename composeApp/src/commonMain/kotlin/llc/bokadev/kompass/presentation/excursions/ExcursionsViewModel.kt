package llc.bokadev.kompass.presentation.excursions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_section_error
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel

class ExcursionsViewModel(
    private val repository: ExcursionRepository,
    private val saved: SavedStateHandle,
    private val favorites: FavoritesRepository,
    private val navigator: Navigator
) : BaseViewModel<ExcursionsState, ExcursionsEvent>() {

    // Filters survive ViewModel recreation through the SavedStateHandle.
    override val initialViewState = ExcursionsState(
        filter = ExcursionFilter(
            interests = saved.get<ArrayList<String>>(KEY_INTERESTS)?.toSet().orEmpty(),
            timeBand = saved[KEY_TIME],
            planningLevel = saved[KEY_PLANNING]
        )
    )
    private var hasLoadedInitialData = false
    private var job: Job? = null

    init {
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                state.update { it.copy(savedKeys = list.map { e -> FavoriteKey(e.type, e.id) }.toSet()) }
            }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val excursionsState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: ExcursionsEvent) {
        when (event) {
            ExcursionsEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            ExcursionsEvent.OnRetryClick -> load()
            is ExcursionsEvent.OnFilterChange -> {
                saved[KEY_INTERESTS] = ArrayList(event.filter.interests)
                saved[KEY_TIME] = event.filter.timeBand
                saved[KEY_PLANNING] = event.filter.planningLevel
                state.update { it.copy(filter = event.filter) }
            }

            is ExcursionsEvent.OnSaveClick -> {
                if (event.saved) favorites.save(event.type, event.id) else favorites.remove(event.type, event.id)
            }

            is ExcursionsEvent.OnExcursionClick -> viewModelScope.launch {
                navigator.navigateTo(
                    if (event.type == FavoriteItemType.PLACE) Screen.PlaceDetailScreen(event.id) else Screen.ExperienceDetailScreen(event.id)
                )
            }
        }
    }

    private fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            state.update { it.copy(isLoading = it.items.isEmpty(), error = null) }
            repository.getCollection()
                .onSuccess { result ->
                    state.update { it.copy(isLoading = false, items = result.items, cached = result.isCached) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.items.isEmpty()) BaseError.InlineError(Res.string.redesign_section_error) else null
                        )
                    }
                }
        }
    }

    private companion object {
        const val KEY_INTERESTS = "interests"
        const val KEY_TIME = "time"
        const val KEY_PLANNING = "planning"
    }
}
