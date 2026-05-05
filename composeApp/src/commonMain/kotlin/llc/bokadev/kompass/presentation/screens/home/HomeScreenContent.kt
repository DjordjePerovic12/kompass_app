package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.IconButton
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
import llc.bokadev.kompass.domain.model.InfoNotice
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
    onNoticeClick: (String) -> Unit,
    onEventsSeeAll: () -> Unit,
    onNearbySeeAll: () -> Unit,
    onInfoCenterSeeAll: () -> Unit,
    onMyGuidesClick: () -> Unit,
    onIntent: (HomeEvent) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()

    if (state.isLoading &&
        state.mustSeePlaces.isEmpty() &&
        state.upcomingEvents.isEmpty() &&
        state.infoCenterNotices.isEmpty() &&
        state.nearbyPlaces.isEmpty()
    ) {
        HomeSplashLoader()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorNavy)
            .verticalScroll(rememberScrollState())
    ) {
        HeroHeader(
            showMyGuides = state.hasPremiumAccess,
            onMyGuidesClick = onMyGuidesClick
        )
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
        if (state.infoCenterNotices.isNotEmpty()) {
            InfoCenterPreviewSection(
                notices = state.infoCenterNotices,
                lang = lang,
                onNoticeClick = onNoticeClick,
                onSeeAll = onInfoCenterSeeAll
            )
        }
        UpcomingEventsSection(
            events = state.upcomingEvents,
            isLoading = state.isLoading,
            lang = lang,
            onEventClick = onEventClick,
            onSeeAll = onEventsSeeAll
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
private fun HomeSplashLoader() {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorNavy)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.92f),
                    radius = size.minDimension * 0.12f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.92f),
                    radius = size.minDimension * 0.28f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            Text(
                text = "KOmpass",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = "Loading Kotor for you…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.68f)
            )
        }
    }
}

@Composable
private fun HeroHeader(
    showMyGuides: Boolean,
    onMyGuidesClick: () -> Unit
) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            }

            if (showMyGuides) {
                IconButton(
                    onClick = onMyGuidesClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.92f),
                            radius = size.minDimension * 0.18f,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.92f),
                            radius = size.minDimension * 0.36f,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
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
    onEventClick: (String) -> Unit,
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
        SectionHeader(title = "Upcoming Events", onSeeAll = onSeeAll)
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

@Composable
private fun InfoCenterPreviewSection(
    notices: List<InfoNotice>,
    lang: String,
    onNoticeClick: (String) -> Unit,
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
        SectionHeader(title = "Recent News", onSeeAll = onSeeAll)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            notices.forEach { notice ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.colorWhite)
                        .border(1.dp, colors.colorSlateGhost, RoundedCornerShape(18.dp))
                        .clickable { onNoticeClick(notice.id) }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = notice.localizedTitle(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.colorNavy
                    )
                    Text(
                        text = notice.localizedShortDescription(lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlate
                    )
                    val meta = listOfNotNull(
                        notice.noticeType.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        notice.startsAt?.toNoticeMetaTime(),
                        notice.localizedLocation(lang).ifBlank { null }
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (notice.priorityRank() == 0) colors.colorError else colors.colorSlateLight
                        )
                    }
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

@OptIn(ExperimentalTime::class)
private fun String.toNoticeMetaTime(): String? = runCatching {
    val dateTime = Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dateTime.date.day}. ${dateTime.date.month.name.take(3)}"
}.getOrNull()

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
