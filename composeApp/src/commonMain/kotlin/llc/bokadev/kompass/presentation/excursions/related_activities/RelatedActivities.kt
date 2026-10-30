package llc.bokadev.kompass.presentation.excursions.related_activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Embedded section of the place detail (not a destination), so it resolves its own ViewModel:
 * keyed by [placeId] so every place gets its own instance, with the id passed via parametersOf.
 * Hidden while loading or when the place has no related activities.
 */
@Composable
fun RelatedActivities(
    placeId: String,
    modifier: Modifier = Modifier,
    viewModel: RelatedActivitiesViewModel = koinViewModel(key = "related_activities:$placeId") { parametersOf(placeId) }
) {
    val state by viewModel.relatedActivitiesState.collectAsStateWithLifecycle()

    RelatedActivitiesContent(
        modifier = modifier,
        state = state,
        onRetryClick = { viewModel.onEvent(RelatedActivitiesEvent.OnRetryClick) },
        onSaveClick = { id, saved -> viewModel.onEvent(RelatedActivitiesEvent.OnSaveClick(id, saved)) },
        onActivityClick = { id -> viewModel.onEvent(RelatedActivitiesEvent.OnActivityClick(id)) }
    )
}
