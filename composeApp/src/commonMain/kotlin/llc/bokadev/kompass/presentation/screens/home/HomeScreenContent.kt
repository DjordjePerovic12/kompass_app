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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.screens.home.components.CompactPlaceCard
import llc.bokadev.kompass.presentation.screens.home.components.EventCard
import llc.bokadev.kompass.presentation.screens.home.components.FeaturedPlaceCard
import llc.bokadev.kompass.presentation.screens.home.components.SectionHeader
import llc.bokadev.kompass.presentation.theme.KompassTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun HomeScreenContent(
    state: HomeState,
    onPlaceClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onNearbySeeAll: () -> Unit,
    onIntent: (HomeEvent) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorNavy)
            .verticalScroll(rememberScrollState())
    ) {
        HeroHeader()
        MustSeeSection(
            places = state.mustSeePlaces,
            isLoading = state.isLoading,
            lang = lang,
            onPlaceClick = onPlaceClick
        )
        if (state.nearbyPlaces.isNotEmpty()) {
            NearbyYouSection(
                nearbyPlaces = state.nearbyPlaces,
                isLoading = state.isLoading,
                isUsingCurrentLocation = state.isUsingCurrentLocation,
                lang = lang,
                onPlaceClick = onPlaceClick,
                onSeeAll = onNearbySeeAll
            )
        }
        UpcomingEventsSection(
            events = state.upcomingEvents,
            isLoading = state.isLoading,
            lang = lang,
            onEventClick = onEventClick
        )
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
    lang: String,
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
                        name = place.localizedName(lang),
                        category = place.category.uiText,
                        zone = place.zone ?: "",
                        imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                        onClick = { onPlaceClick(place.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyYouSection(
    nearbyPlaces: List<NearbyPlace>,
    isLoading: Boolean,
    isUsingCurrentLocation: Boolean,
    lang: String,
    onPlaceClick: (String) -> Unit,
    onSeeAll: () -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorSurface)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Nearby You", onSeeAll = onSeeAll)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isUsingCurrentLocation) {
                Text(
                    text = "Location unavailable, showing closest curated spots around Old Town.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.colorSlate
                )
            }

            if (isLoading && nearbyPlaces.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = colors.colorAmber
                )
            } else if (nearbyPlaces.isEmpty()) {
                Text(
                    text = "No nearby places available right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate
                )
            } else {
                nearbyPlaces.forEach { nearbyPlace ->
                    val place = nearbyPlace.place
                    CompactPlaceCard(
                        name = place.localizedName(lang),
                        category = place.category.uiText,
                        zone = place.zone ?: "Kotor",
                        distance = nearbyPlace.distanceKm.toDistanceLabel(),
                        meta = nearbyPlace.toNearbyMeta(),
                        imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                        onClick = { onPlaceClick(place.id) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTime::class)
private fun UpcomingEventsSection(
    events: List<Event>,
    isLoading: Boolean,
    lang: String,
    onEventClick: (String) -> Unit
) {
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
            if (isLoading && events.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = colors.colorAmber
                )
            } else {
                events.forEach { event ->
                    EventCard(
                        name = event.localizedName(lang),
                        venue = event.localizedVenue(lang),
                        day = event.startTime.toHomeEventDay(),
                        month = event.startTime.toHomeEventMonth(),
                        meta = event.toHomeEventMeta(),
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun String.toHomeEventDay(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .day
        .toString()
}.getOrDefault("--")

@OptIn(ExperimentalTime::class)
private fun String.toHomeEventMonth(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .month
        .name
        .take(3)
}.getOrDefault("---")

private fun Double.toDistanceLabel(): String {
    return if (this < 1.0) {
        "${(this * 1000).roundToInt()} m"
    } else {
        "${((this * 10).roundToInt() / 10.0)} km"
    }
}

private fun NearbyPlace.toNearbyMeta(): String {
    val distanceText = when {
        distanceKm < 1.0 -> "${(distanceKm * 12).roundToInt()} min walk"
        distanceKm <= 5.0 -> "${(distanceKm * 3).roundToInt()} min drive"
        else -> "${(distanceKm * 2.2).roundToInt()} min drive"
    }

    val bestTimeText = when (place.bestTime.name.lowercase()) {
        "morning" -> "Best in morning"
        "afternoon" -> "Best in afternoon"
        "evening" -> "Best in evening"
        "night" -> "Best at night"
        else -> null
    }

    return listOfNotNull(distanceText, bestTimeText).joinToString(" · ")
}

private fun Event.toHomeEventMeta(): String {
    val start = startTime.toHomeEventTime()
    val end = endTime?.toHomeEventTime()
    return when {
        start != null && end != null -> "$start – $end"
        start != null -> start
        else -> ""
    }
}

@OptIn(ExperimentalTime::class)
private fun String.toHomeEventTime(): String? = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .let { time ->
            "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
        }
}.getOrNull()
