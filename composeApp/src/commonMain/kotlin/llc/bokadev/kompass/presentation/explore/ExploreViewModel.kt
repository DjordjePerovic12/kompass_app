@file:OptIn(kotlin.time.ExperimentalTime::class)

package llc.bokadev.kompass.presentation.explore

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.domain.model.window
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetUpcomingEventsUseCase
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import kotlin.time.Clock

class ExploreViewModel(
    private val getMustSeePlaces: GetMustSeePlacesUseCase,
    private val excursions: ExcursionRepository,
    private val getLocalFinds: GetLocalFindsUseCase,
    private val getUpcomingEvents: GetUpcomingEventsUseCase,
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost,
    private val analytics: AnalyticsRepository
) : BaseViewModel<ExploreState, ExploreEvent>() {

    override val initialViewState = ExploreState()
    private var hasLoadedInitialData = false

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val exploreState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("browse")
                loadAll()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: ExploreEvent) {
        when (event) {
            ExploreEvent.OnRefresh -> loadAll()
            ExploreEvent.OnSearchClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.SearchScreen)
            }

            ExploreEvent.OnLanguageClick -> languageSheet.open()
            is ExploreEvent.OnDestinationClick -> open(event.destination)
        }
    }

    private fun open(destination: ExploreDestination) {
        val categoryKey = when (destination) {
            ExploreDestination.PLACES -> "see_and_visit"
            ExploreDestination.EAT_AND_DRINK -> "eat_and_drink"
            ExploreDestination.ACTIVITIES -> "activities"
            ExploreDestination.WORTH_THE_DRIVE -> "worth_the_drive"
            ExploreDestination.STORIES -> "hidden_gems"
            else -> null
        }
        categoryKey?.let { analytics.trackCategoryView(it, contentOrigin = "browse") }
        viewModelScope.launch {
            navigator.navigateTo(
                when (destination) {
                    ExploreDestination.PLACES -> Screen.CategoryListScreen(CategoryListKind.PLACES.name)
                    ExploreDestination.EAT_AND_DRINK -> Screen.CategoryListScreen(CategoryListKind.EAT_AND_DRINK.name)
                    ExploreDestination.ACTIVITIES -> Screen.CategoryListScreen(CategoryListKind.ACTIVITIES.name)
                    ExploreDestination.WORTH_THE_DRIVE -> Screen.WorthTheDriveScreen
                    ExploreDestination.STORIES -> Screen.LocalFindsScreen
                    ExploreDestination.EVENTS -> Screen.EventsScreen
                    ExploreDestination.LOCAL_UPDATES -> Screen.InfoCenterScreen
                    ExploreDestination.SERVICES -> Screen.ServicesScreen
                }
            )
        }
    }

    private fun loadAll() {
        viewModelScope.launch {
            getMustSeePlaces().onSuccess { places ->
                state.update { it.copy(heroPhoto = places.firstNotNullOfOrNull { p -> p.photos.firstOrNull() }) }
            }
        }
        viewModelScope.launch {
            excursions.getCollection().onSuccess { collection ->
                state.update {
                    it.copy(
                        hasWorthTheDrive = collection.items.isNotEmpty(),
                        worthTheDrivePhoto = collection.items.firstNotNullOfOrNull { entry -> entry.photo }
                    )
                }
            }
        }
        viewModelScope.launch {
            getLocalFinds().onSuccess { finds ->
                state.update {
                    it.copy(
                        hasStories = finds.isNotEmpty(),
                        storiesHaveAudio = finds.any { find -> find.audioFile?.values?.any { path -> path.isNotBlank() } == true }
                    )
                }
            }
        }
        viewModelScope.launch {
            getUpcomingEvents().onSuccess { events ->
                // "Next" means not started yet; an ongoing festival's start date would read as the past.
                val now = Clock.System.now()
                val next = events.mapNotNull { e -> e.window()?.start?.takeIf { it >= now }?.let { it to e } }.minByOrNull { it.first }?.second
                state.update { it.copy(nextEventStart = next?.startTime) }
            }
        }
        viewModelScope.launch {
            getCurrentInfoNotices().onSuccess { notices ->
                state.update { it.copy(hasImportantNotice = notices.any { notice -> notice.priorityRank() <= 1 }) }
            }
        }
    }
}
