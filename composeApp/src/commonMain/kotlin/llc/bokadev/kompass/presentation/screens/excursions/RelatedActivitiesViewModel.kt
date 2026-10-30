package llc.bokadev.kompass.presentation.screens.excursions

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.core.navigation.Screen

class RelatedActivitiesViewModel(
    private val repository: ExcursionRepository,
    private val favorites: FavoritesRepository,
    private val navigator: Navigator
) : BaseViewModel<RelatedActivitiesState, RelatedActivitiesAction, Nothing>(RelatedActivitiesState()) {
    private var loadedId: String? = null
    private var job: Job? = null

    init {
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                updateState { it.copy(savedActivityIds = list.filter { e -> e.type == FavoriteItemType.ACTIVITY }.map { e -> e.id }.toSet()) }
            }
        }
    }

    override fun onAction(action: RelatedActivitiesAction) {
        when (action) {
            is RelatedActivitiesAction.Load -> load(action.placeId, action.force)
            is RelatedActivitiesAction.SetSaved ->
                if (action.saved) favorites.save(FavoriteItemType.ACTIVITY, action.activityId)
                else favorites.remove(FavoriteItemType.ACTIVITY, action.activityId)
            is RelatedActivitiesAction.Open -> navigator.navigateTo(Screen.ExperienceDetailScreen(action.activityId))
        }
    }

    private fun load(id: String, force: Boolean) {
        if (id == loadedId && !force) return
        loadedId = id
        job?.cancel()
        updateState { it.copy(items = emptyList(), cached = false, error = null, isLoading = true) }
        job = viewModelScope.launch {
            repository.getRelatedActivities(id)
                .onSuccess { result -> updateState { it.copy(items = result.items, cached = result.isCached, isLoading = false) } }
                .onError { _, _ -> updateState { it.copy(isLoading = false, error = UiText { s -> s.redesign.sectionError }) } }
        }
    }
}
