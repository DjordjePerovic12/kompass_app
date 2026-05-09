package llc.bokadev.kompass.presentation.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.AppStrings
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Category
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorDustySage
import llc.bokadev.kompass.presentation.theme.colorMutedSky
import llc.bokadev.kompass.presentation.theme.colorRoseClay
import llc.bokadev.kompass.presentation.theme.colorSoftTerracotta

private val CATEGORY_SUBTITLES = mapOf(
    "activities" to "Things to do & experiences",
    "hidden_gems" to "Less obvious corners & local favourites",
    "practical" to "Transport, tips & essentials"
)

private data class BrowseCategoryCardData(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val prominent: Boolean,
    val onClick: () -> Unit
)

@Composable
fun CategoriesScreenContent(
    state: CategoriesState,
    onIntent: (CategoriesEvent) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onPlacesClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    strings: AppStrings,
    onServicesClick: () -> Unit = {},
    onEventsClick: () -> Unit = {},
    onInfoCenterClick: () -> Unit = {}
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val groupedCategories = state.categories.associateBy { it.id }
    val categoryCards = listOfNotNull(
        BrowseCategoryCardData(
            id = "favorites",
            title = "My Favorites",
            subtitle = "Saved places & activities",
            accent = colorSoftTerracotta,
            prominent = true,
            onClick = onFavoritesClick
        ),
        BrowseCategoryCardData(
            id = "places",
            title = "Places",
            subtitle = "Must-sees, restaurants & landmarks",
            accent = colorDustySage,
            prominent = true,
            onClick = onPlacesClick
        ),
        groupedCategories["activities"]?.let {
            BrowseCategoryCardData(
                id = it.id,
                title = it.localizedName(lang).prettyCategoryLabel(),
                subtitle = CATEGORY_SUBTITLES[it.id].orEmpty(),
                accent = colorMutedSky,
                prominent = false,
                onClick = { onCategoryClick(it) }
            )
        },
        groupedCategories["hidden_gems"]?.let {
            BrowseCategoryCardData(
                id = it.id,
                title = "Local Finds",
                subtitle = CATEGORY_SUBTITLES[it.id].orEmpty(),
                accent = colorRoseClay,
                prominent = false,
                onClick = { onCategoryClick(it) }
            )
        }
    )
    val practicalCategory = groupedCategories["practical"]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorSurface)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.colorOrangeMain
                )
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.colorWhite)
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.couldNotLoadCategories,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.colorNavy
                    )
                    Text(
                        text = strings.retry,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.colorOrangeMain,
                        modifier = Modifier.clickable { onIntent(CategoriesEvent.Retry) }
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 14.dp,
                        bottom = 104.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryCards) { card ->
                        CategoryTile(
                            title = card.title,
                            subtitle = card.subtitle,
                            accent = card.accent,
                            prominent = card.prominent,
                            onClick = card.onClick
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        UtilityEditorialRow(
                            title = strings.eventsTitle,
                            subtitle = strings.eventsSubtitle,
                            markerColor = colorMutedSky,
                            onClick = onEventsClick
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        UtilityEditorialRow(
                            title = strings.infoCenterTitle,
                            subtitle = strings.infoCenterSubtitle,
                            markerColor = colorRoseClay,
                            onClick = onInfoCenterClick
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        UtilityEditorialRow(
                            title = strings.servicesTitle,
                            subtitle = strings.servicesSubtitle,
                            markerColor = colors.colorOrangeMain.copy(alpha = 0.72f),
                            onClick = onServicesClick
                        )
                    }

                    practicalCategory?.let { category ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            UtilityEditorialRow(
                                title = category.localizedName(lang).prettyCategoryLabel(),
                                subtitle = CATEGORY_SUBTITLES[category.id].orEmpty(),
                                markerColor = colorDustySage.copy(alpha = 0.92f),
                                onClick = { onCategoryClick(category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    title: String,
    subtitle: String,
    accent: Color,
    prominent: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.07f)
            )
            .clip(shape)
            .background(accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 35.dp),
    ) {
        Column(
            modifier = Modifier
                .align(if (prominent) Alignment.BottomStart else Alignment.CenterStart)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (prominent) 24.sp else 21.sp,
                    lineHeight = if (prominent) 28.sp else 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.4).sp
                ),
                color = colors.colorNavy,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.colorNavy.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UtilityEditorialRow(
    title: String,
    subtitle: String,
    markerColor: Color,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.colorWhite)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(markerColor)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                ),
                color = colors.colorNavy
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = colors.colorNavy.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "↗",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.colorNavy.copy(alpha = 0.48f)
        )
    }
}

private fun String.prettyCategoryLabel(): String =
    replace('&', ' ')
        .replace("  ", " ")
        .trim()
