package llc.bokadev.kompass.presentation.excursions.related_activities

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
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel

/** One instance per place: [RelatedActivities] keys it by [placeId] and passes the id via parametersOf. */
class RelatedActivitiesViewModel(
    private val placeId: String,
    private val repository: ExcursionRepository,
    private val favorites: FavoritesRepository,
    private val navigator: Navigator
) : BaseViewModel<RelatedActivitiesState, RelatedActivitiesEvent>() {

    override val initialViewState = RelatedActivitiesState()
    private var hasLoadedInitialData = false
    private var job: Job? = null

    init {
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                state.update { it.copy(savedActivityIds = list.filter { e -> e.type == FavoriteItemType.ACTIVITY }.map { e -> e.id }.toSet()) }
            }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val relatedActivitiesState = state
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

    fun onEvent(event: RelatedActivitiesEvent) {
        when (event) {
            RelatedActivitiesEvent.OnRetryClick -> load()
            is RelatedActivitiesEvent.OnSaveClick -> {
                if (event.saved) favorites.save(FavoriteItemType.ACTIVITY, event.activityId)
                else favorites.remove(FavoriteItemType.ACTIVITY, event.activityId)
            }

            is RelatedActivitiesEvent.OnActivityClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.ExperienceDetailScreen(event.activityId))
            }
        }
    }

    /** Starts from a clean section (retry included), as before: no stale rows or cached note. */
    private fun load() {
        job?.cancel()
        state.update { it.copy(isLoading = true, error = null, items = emptyList(), cached = false) }
        job = viewModelScope.launch {
            repository.getRelatedActivities(placeId)
                .onSuccess { result ->
                    state.update { it.copy(isLoading = false, items = result.items, cached = result.isCached) }
                }
                .onError { _, _ ->
                    state.update { it.copy(isLoading = false, error = BaseError.InlineError(Res.string.redesign_section_error)) }
                }
        }
    }
}
