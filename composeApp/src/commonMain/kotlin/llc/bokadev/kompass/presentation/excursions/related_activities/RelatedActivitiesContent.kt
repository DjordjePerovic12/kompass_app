package llc.bokadev.kompass.presentation.excursions.related_activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.excursions_cached
import kompass.composeapp.generated.resources.excursions_related
import kompass.composeapp.generated.resources.retry
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.presentation.excursions.components.ExcursionRow
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource

/** Draws nothing while loading or when there are no related activities and nothing failed. */
@Composable
fun RelatedActivitiesContent(
    modifier: Modifier = Modifier,
    state: RelatedActivitiesState,
    onRetryClick: () -> Unit,
    onSaveClick: (activityId: String, saved: Boolean) -> Unit,
    onActivityClick: (activityId: String) -> Unit
) {
    val colors = KompassTheme.colors
    val failure = state.error
    if (state.items.isEmpty() && failure == null) return
    Column(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.excursions_related), style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite)
        if (state.cached) Text(stringResource(Res.string.excursions_cached), style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
        if (failure != null) Text(stringResource(failure.messageRes), style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
        if (failure != null || state.cached) {
            TextButton(onClick = onRetryClick) { Text(stringResource(Res.string.retry), color = colors.lampYellow) }
        }
        state.items.forEach { activity ->
            key(activity.id) {
                val saved = activity.id in state.savedActivityIds
                ExcursionRow(
                    Excursion(activity.id, activity = activity), saved,
                    { onSaveClick(activity.id, !saved) },
                    { onActivityClick(activity.id) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun RelatedActivitiesContentPreview() {
    KompassTheme {
        RelatedActivitiesContent(
            state = RelatedActivitiesState(
                isLoading = false,
                items = listOf(
                    Experience(id = "islands", cityId = "kotor", name = mapOf("en" to "Perast islands"), description = emptyMap(), photos = emptyList())
                ),
                cached = true,
                savedActivityIds = setOf("islands")
            ),
            onRetryClick = {},
            onSaveClick = { _, _ -> },
            onActivityClick = {}
        )
    }
}
