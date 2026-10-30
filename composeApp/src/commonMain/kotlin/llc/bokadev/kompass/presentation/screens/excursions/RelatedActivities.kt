package llc.bokadev.kompass.presentation.screens.excursions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.presentation.screens.excursions.components.ExcursionRow
import org.koin.compose.viewmodel.koinViewModel
import llc.bokadev.kompass.ui.theme.KompassTheme

/** Embedded section: hidden while loading or when the place has no related activities. */
@Composable
fun RelatedActivities(placeId: String) {
    val vm: RelatedActivitiesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    LaunchedEffect(placeId) { vm.onAction(RelatedActivitiesAction.Load(placeId)) }
    RelatedActivitiesContent(placeId, state, vm::onAction)
}

@Composable
private fun RelatedActivitiesContent(placeId: String, state: RelatedActivitiesState, onAction: (RelatedActivitiesAction) -> Unit) {
    val app = rememberAppStrings()
    val colors = KompassTheme.colors
    val failure = state.error
    if (state.items.isEmpty() && failure == null) return
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(app.excursions.related, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite)
        if (state.cached) Text(app.excursions.cached, style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
        if (failure != null) Text(failure.resolve(app), style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
        if (failure != null || state.cached) {
            TextButton(onClick = { onAction(RelatedActivitiesAction.Load(placeId, force = true)) }) { Text(app.retry, color = colors.lampYellow) }
        }
        state.items.forEach { activity ->
            key(activity.id) {
                val saved = activity.id in state.savedActivityIds
                ExcursionRow(
                    Excursion(activity.id, activity = activity), saved,
                    { onAction(RelatedActivitiesAction.SetSaved(activity.id, !saved)) },
                    { onAction(RelatedActivitiesAction.Open(activity.id)) }
                )
            }
        }
    }
}
