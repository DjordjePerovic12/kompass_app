package llc.bokadev.kompass.presentation.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.toComposeColor
import llc.bokadev.kompass.domain.model.Category
import llc.bokadev.kompass.presentation.theme.KompassTheme

private val CATEGORY_SUBTITLES = mapOf(
    "eat_and_drink" to "Restaurants, bars & cafés",
    "see_and_visit"  to "Landmarks & heritage sites",
    "activities"     to "Things to do & experiences",
    "hidden_gems"    to "Local secrets & off-beat spots",
    "practical"      to "Transport, tips & essentials"
)

private val FALLBACK_COLORS = listOf(
    Color(0xFFF59E0BL.toInt()), // amber
    Color(0xFF243B53L.toInt()), // navy/800
    Color(0xFF059669L.toInt()), // green
    Color(0xFF486581L.toInt()), // slate/600
    Color(0xFF829AB1L.toInt())  // slate/400
)

@Composable
fun CategoriesScreenContent(
    state: CategoriesState,
    onIntent: (CategoriesEvent) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onServicesClick: () -> Unit = {},
    onEventsClick: () -> Unit = {},
    onInfoCenterClick: () -> Unit = {}
) {
    val colors = KompassTheme.colors

    Column(modifier = Modifier.fillMaxSize().background(colors.colorSurface)) {
        // Navy header


        // Content
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.colorAmber)
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Could not load categories", color = colors.colorSlate)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Retry",
                            color = colors.colorAmberDark,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onIntent(CategoriesEvent.Retry) }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.colorWhite)
                ) {
                    item {
                        UtilityRow(
                            letter = "E",
                            title = "Events",
                            subtitle = "What is happening today, this week, and this month",
                            accent = Color(0xFF243B53),
                            onClick = onEventsClick
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp, end = 20.dp),
                            color = colors.colorSlateGhost
                        )
                    }
                    itemsIndexed(state.categories) { index, category ->
                        CategoryRow(
                            category = category,
                            fallbackColor = FALLBACK_COLORS.getOrElse(index) { Color.Gray },
                            onClick = { onCategoryClick(category) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp, end = 20.dp),
                            color = colors.colorSlateGhost
                        )
                    }
                    item {
                        UtilityRow(
                            letter = "I",
                            title = "Info Center",
                            subtitle = "Town notices, disruptions, and practical updates",
                            accent = Color(0xFF9F1239),
                            onClick = onInfoCenterClick
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp, end = 20.dp),
                            color = colors.colorSlateGhost
                        )
                    }
                    item {
                        ServicesRow(onClick = onServicesClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicesRow(onClick: () -> Unit) {
    UtilityRow(
        letter = "S",
        title = "Services",
        subtitle = "Rent-a-car, boat rentals & more",
        accent = Color(0xFF334E68),
        onClick = onClick
    )
}

@Composable
private fun UtilityRow(
    letter: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = accent
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorSlate
            )
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.colorAmberDark
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    fallbackColor: Color,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val accentColor = category.color?.let {
        runCatching { it.toComposeColor() }.getOrNull()
    } ?: fallbackColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored initial circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.localizedName("en").first().uppercaseChar().toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }

        // Text
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = category.localizedName("en"),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
            Text(
                text = CATEGORY_SUBTITLES[category.id] ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorSlate
            )
        }

        // Amber chevron
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.colorAmberDark
        )
    }
}
