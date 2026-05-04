package llc.bokadev.kompass.presentation.screens.experiences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun ExperiencesScreenContent(
    state: ExperiencesState,
    onIntent: (ExperiencesEvent) -> Unit,
    onActivityClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val categories = state.activities.mapNotNull { it.category?.takeIf(String::isNotBlank) }.distinct()
    val filteredActivities = state.activities.filter { activity ->
        state.selectedCategory == null || activity.category == state.selectedCategory
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorSurface),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Find hikes, UNESCO trails, medieval fortresses, village detours, and sunset ideas that move visitors outside the usual Kotor core.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate
                )
                if (categories.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            ActivityCategoryChip(
                                label = "All",
                                isSelected = state.selectedCategory == null,
                                onClick = { onIntent(ExperiencesEvent.SelectCategory(null)) }
                            )
                        }
                        items(categories) { category ->
                            ActivityCategoryChip(
                                label = category.prettyActivityCategory(),
                                isSelected = state.selectedCategory == category,
                                onClick = { onIntent(ExperiencesEvent.SelectCategory(category)) }
                            )
                        }
                    }
                }
            }
        }

        when {
            state.error != null -> item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.colorError
                    )
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.colorAmberDark,
                        modifier = Modifier.clickable { onIntent(ExperiencesEvent.Retry) }
                    )
                }
            }

            !state.isLoading && filteredActivities.isEmpty() -> item {
                Text(
                    text = if (state.selectedCategory == null) "No activities available yet." else "No activities match this category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            else -> {
                items(filteredActivities, key = { it.id }) { activity ->
                    ActivityCard(
                        activity = activity,
                        lang = lang,
                        onClick = { onActivityClick(activity.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) colors.colorNavy else colors.filterUnselectedSurface,
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .noRippleClickable(onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) colors.colorWhite else colors.filterUnselectedText
        )
    }
}

@Composable
private fun ActivityCard(
    activity: Experience,
    lang: String,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp)
            .background(colors.colorWhite, shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        activity.category?.takeIf(String::isNotBlank)?.let {
            Text(
                text = it.prettyActivityCategory(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorAmberDark
            )
        }
        Text(
            text = activity.localizedName(lang),
            style = MaterialTheme.typography.titleLarge,
            color = colors.colorNavy
        )
        Text(
            text = activity.localizedDescription(lang),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.colorSlate,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = listOfNotNull(
                activity.localizedLocation(lang).takeIf(String::isNotBlank),
                activity.durationMin?.let { "$it min" },
                activity.bestTime.toUiLabel()
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = colors.colorSlateLight
        )
        if (activity.audioFile != null || activity.localizedLongDescription(lang).isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Premium extras available",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
        }
    }
}

private fun String.prettyActivityCategory(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

private fun llc.bokadev.kompass.domain.model.BestTime.toUiLabel(): String = when (this) {
    llc.bokadev.kompass.domain.model.BestTime.MORNING -> "Best in morning"
    llc.bokadev.kompass.domain.model.BestTime.AFTERNOON -> "Best in afternoon"
    llc.bokadev.kompass.domain.model.BestTime.EVENING -> "Best in evening"
    llc.bokadev.kompass.domain.model.BestTime.ANYTIME -> "Any time"
}
