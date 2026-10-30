@file:OptIn(kotlin.time.ExperimentalTime::class)

package llc.bokadev.kompass.presentation.screens.categories

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.domain.model.window
import kotlin.time.Clock
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetUpcomingEventsUseCase

class CategoriesViewModel(
    private val getMustSeePlaces: GetMustSeePlacesUseCase,
    private val excursions: ExcursionRepository,
    private val getLocalFinds: GetLocalFindsUseCase,
    private val getUpcomingEvents: GetUpcomingEventsUseCase,
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost,
    private val analytics: AnalyticsRepository
) : BaseViewModel<CategoriesState, CategoriesAction, Nothing>(CategoriesState()) {

    override suspend fun onFirstStart() {
        analytics.trackScreenView("browse")
        loadAll()
    }

    override fun onAction(action: CategoriesAction) {
        when (action) {
            CategoriesAction.Refresh -> loadAll()
            CategoriesAction.OpenSearch -> navigator.navigateTo(Screen.SearchScreen)
            CategoriesAction.OpenLanguage -> languageSheet.open()
            is CategoriesAction.Open -> open(action.destination)
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

    private fun loadAll() {
        viewModelScope.launch {
            getMustSeePlaces().onSuccess { places ->
                updateState { it.copy(heroPhoto = places.firstNotNullOfOrNull { p -> p.photos.firstOrNull() }) }
            }
        }
        viewModelScope.launch {
            excursions.getCollection().onSuccess { collection ->
                updateState {
                    it.copy(
                        hasWorthTheDrive = collection.items.isNotEmpty(),
                        worthTheDrivePhoto = collection.items.firstNotNullOfOrNull { entry -> entry.photo }
                    )
                }
            }
        }
        viewModelScope.launch {
            getLocalFinds().onSuccess { finds ->
                updateState {
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
                updateState { it.copy(nextEventStart = next?.startTime) }
            }
        }
        viewModelScope.launch {
            getCurrentInfoNotices().onSuccess { notices ->
                updateState { it.copy(hasImportantNotice = notices.any { notice -> notice.priorityRank() <= 1 }) }
            }
        }
    }
}
