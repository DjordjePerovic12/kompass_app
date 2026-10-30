package llc.bokadev.kompass.presentation.screens.categories

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

/** Explore's entry points. */
enum class ExploreDestination { PLACES, EAT_AND_DRINK, ACTIVITIES, WORTH_THE_DRIVE, STORIES, EVENTS, LOCAL_UPDATES, SERVICES }

sealed interface CategoriesAction : BaseAction {
    data class Open(val destination: ExploreDestination) : CategoriesAction
    data object OpenSearch : CategoriesAction
    data object OpenLanguage : CategoriesAction
    data object Refresh : CategoriesAction
}
