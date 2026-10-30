package llc.bokadev.kompass.presentation.screens.excursions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.navigation.Route

class ExcursionsViewModel(
    private val repository: ExcursionRepository,
    private val saved: SavedStateHandle,
    private val favorites: FavoritesRepository,
    private val navigator: Navigator
) : BaseViewModel<ExcursionsState, ExcursionsAction, Nothing>(
    ExcursionsState(
        filter = ExcursionFilter(saved.get<ArrayList<String>>("interests")?.toSet().orEmpty(), saved["time"], saved["planning"])
    )
) {
    private var job: Job? = null

    init {
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list -> updateState { it.copy(savedKeys = list.map { e -> FavoriteKey(e.type, e.id) }.toSet()) } }
        }
    }

    override suspend fun onFirstStart() = load()

    override fun onAction(action: ExcursionsAction) {
        when (action) {
            ExcursionsAction.Reload -> load()
            is ExcursionsAction.Filter -> {
                saved["interests"] = ArrayList(action.value.interests)
                saved["time"] = action.value.timeBand
                saved["planning"] = action.value.planningLevel
                updateState { it.copy(filter = action.value) }
            }
            is ExcursionsAction.SetSaved ->
                if (action.saved) favorites.save(action.type, action.id) else favorites.remove(action.type, action.id)
            is ExcursionsAction.Open -> navigator.navigateTo(
                if (action.type == FavoriteItemType.PLACE) Route.PlaceDetail(action.id) else Route.ExperienceDetail(action.id)
            )
            ExcursionsAction.Back -> navigator.navigateUp()
        }
    }

    private fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            updateState { it.copy(isLoading = it.items.isEmpty(), error = null) }
            repository.getCollection().fold(
                onSuccess = { result -> updateState { it.copy(items = result.items, cached = result.isCached, isLoading = false) } },
                onFailure = { updateState { it.copy(isLoading = false, error = if (it.items.isEmpty()) LOAD_FAILED else null) } }
            )
        }
    }

    private companion object {
        val LOAD_FAILED = UiText { it.redesign.sectionError }
    }
}
