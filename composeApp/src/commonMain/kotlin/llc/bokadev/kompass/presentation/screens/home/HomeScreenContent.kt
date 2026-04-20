package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.screens.home.components.CompactPlaceCard
import llc.bokadev.kompass.presentation.screens.home.components.EventCard
import llc.bokadev.kompass.presentation.screens.home.components.FeaturedPlaceCard
import llc.bokadev.kompass.presentation.screens.home.components.SectionHeader
import llc.bokadev.kompass.presentation.theme.KompassTheme

private data class PlacePreview(
    val id: String,
    val name: String,
    val category: String,
    val zone: String,
    val distance: String = ""
)

private data class EventPreview(
    val id: String,
    val name: String,
    val venue: String,
    val day: String,
    val month: String
)

private val nearbyPlaces = listOf(
    PlacePreview("4", "Luna Restaurant", "Eat & Drink", "Old Town", "0.3 km"),
    PlacePreview("5", "Kotor Bay Kayaking", "Activities", "Marina", "0.8 km"),
)

private val upcomingEvents = listOf(
    EventPreview("1", "Kotor Carnival", "Old Town Square", "14", "FEB"),
    EventPreview("2", "Summer Jazz Night", "City Ramparts", "12", "JUL"),
)

@Composable
fun HomeScreenContent(
    state: HomeState,
    onPlaceClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorNavy)
            .verticalScroll(rememberScrollState())
    ) {
        HeroHeader()
        MustSeeSection(places = state.mustSeePlaces, isLoading = state.isLoading, onPlaceClick = onPlaceClick)
        NearbyYouSection(onPlaceClick = onPlaceClick)
        UpcomingEventsSection(onEventClick = onEventClick)
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colors.colorSurface)
        )
    }
}

@Composable
private fun HeroHeader() {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Kotor, Montenegro",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = "Explore\nthe Old Town",
            style = MaterialTheme.typography.displayLarge,
            lineHeight = 38.sp,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 5.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            Text(
                text = "Search places, events…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun MustSeeSection(
    places: List<Place>,
    isLoading: Boolean,
    onPlaceClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.colorSurface)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Must See", onSeeAll = {})
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp),
                color = colors.colorAmber
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(places) { place ->
                    FeaturedPlaceCard(
                        name = place.localizedName("en"),
                        category = place.category.name,
                        zone = place.zone ?: "",
                        onClick = { onPlaceClick(place.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyYouSection(onPlaceClick: (String) -> Unit) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorSurface)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Nearby You", onSeeAll = {})
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            nearbyPlaces.forEach { place ->
                CompactPlaceCard(
                    name = place.name,
                    category = place.category,
                    zone = place.zone,
                    distance = place.distance,
                    onClick = { onPlaceClick(place.id) }
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventsSection(onEventClick: (String) -> Unit) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorSurface)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Upcoming Events", onSeeAll = {})
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            upcomingEvents.forEach { event ->
                EventCard(
                    name = event.name,
                    venue = event.venue,
                    day = event.day,
                    month = event.month,
                    onClick = { onEventClick(event.id) }
                )
            }
        }
    }
}
