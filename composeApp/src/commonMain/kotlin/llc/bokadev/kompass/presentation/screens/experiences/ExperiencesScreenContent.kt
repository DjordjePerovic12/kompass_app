package llc.bokadev.kompass.presentation.screens.experiences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.favoriteExperiencesFirst
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.screens.experiences.components.ActivityListItem
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject

@Composable
fun ExperiencesScreenContent(
    state: ExperiencesState,
    onIntent: (ExperiencesEvent) -> Unit,
    onActivityClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()
    val favoriteKeys = favoritesRepository.getFavoriteKeySet()
    val categories = state.activities.mapNotNull { it.category?.takeIf(String::isNotBlank) }.distinct()
    val filteredActivities = state.activities
        .filter { activity ->
            state.selectedCategory == null || activity.category == state.selectedCategory
        }
        .favoriteExperiencesFirst(favoriteKeys)

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
                    text = strings.activitiesIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate
                )
                if (categories.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            ActivityCategoryChip(
                                label = strings.all,
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
                        text = strings.retry,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.colorAmberDark,
                        modifier = Modifier.noRippleClickable { onIntent(ExperiencesEvent.Retry) }
                    )
                }
            }

            !state.isLoading && filteredActivities.isEmpty() -> item {
                Text(
                    text = if (state.selectedCategory == null) strings.noActivities else strings.noActivitiesForCategory,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            else -> {
                items(filteredActivities, key = { it.id }) { activity ->
                    ActivityListItem(
                        activity = activity,
                        lang = lang,
                        isFavorited = favoritesRepository.isFavorited(FavoriteItemType.ACTIVITY, activity.id),
                        onFavoriteClick = { favoritesRepository.toggleFavorite(FavoriteItemType.ACTIVITY, activity.id) },
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

private fun String.prettyActivityCategory(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
