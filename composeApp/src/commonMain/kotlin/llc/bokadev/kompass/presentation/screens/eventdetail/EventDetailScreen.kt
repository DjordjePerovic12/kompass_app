package llc.bokadev.kompass.presentation.screens.eventdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.presentation.screens.events.toEventDay
import llc.bokadev.kompass.presentation.screens.events.toEventMeta
import llc.bokadev.kompass.presentation.screens.events.toEventMonth
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.DetailNativeMapCard
import llc.bokadev.kompass.presentation.theme.KompassTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@Composable
fun EventDetailScreen(
    id: String,
    onBack: () -> Unit = {}
) {
    val vm: EventDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()

    EventDetailScreenContent(
        state = state,
        onIntent = vm::onIntent,
        onBack = onBack
    )
}

@Composable
private fun EventDetailScreenContent(
    state: EventDetailState,
    onIntent: (EventDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors

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
                Text(
                    text = state.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .clickable { onIntent(EventDetailEvent.Retry) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy
                )
            }

            state.event != null -> {
                EventDetailBody(
                    event = state.event,
                    onBack = onBack
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalTime::class)
@Composable
private fun EventDetailBody(
    event: Event,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val uriHandler = LocalUriHandler.current
    val eventName = event.localizedName(lang)
    val venue = event.localizedVenue(lang)
    val eventDate = runCatching {
        val localDateTime = Instant.parse(event.startTime).toLocalDateTime(TimeZone.currentSystemDefault())
        "${event.startTime.toEventDay()} ${event.startTime.toEventMonth().uppercase()} · ${localDateTime.date.year}"
    }.getOrDefault("${event.startTime.toEventDay()} ${event.startTime.toEventMonth().uppercase()}")
    val metaLine = listOfNotNull(
        event.category.prettyEventLabel(),
        venue.takeIf { it.isNotBlank() }
    ).joinToString(" · ")
    val practicalLine = listOfNotNull(
        eventDate.takeIf { it.isNotBlank() },
        event.toEventMeta().takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    val chips = listOfNotNull(
        event.category.prettyEventLabel(),
        event.startTime.toEventTimeLabel(),
        event.endTime?.toEventTimeLabel()?.let { "Until $it" },
        event.price?.takeIf { it.isNotBlank() }
    ).distinct().take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                PlacePhotoHeader(
                    imageUrls = event.photos.map(::buildPhotoUrl),
                    imageAspectRatio = 0.94f
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OverlayCircleButton(symbol = "‹", onClick = onBack)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 14.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.14f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.colorWhite)
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = eventName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 31.sp,
                            lineHeight = 35.sp,
                            letterSpacing = (-0.5).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.colorNavy
                    )
                    if (metaLine.isNotBlank()) {
                        Text(
                            text = metaLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.colorNavy.copy(alpha = 0.62f)
                        )
                    }
                    if (practicalLine.isNotBlank()) {
                        Text(
                            text = practicalLine,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.colorOrangeMain
                        )
                    }
                }

                if (chips.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chips.forEachIndexed { index, chip ->
                            InfoChip(label = chip, amber = index == 0)
                        }
                    }
                }

                DetailNativeMapCard(
                    placeName = eventName,
                    summary = "Orient yourself around the venue and see where the event sits in the city.",
                    destination = GeoPoint(event.latitude, event.longitude)
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                DetailSection(
                    title = "What’s Happening",
                    body = event.localizedDescription(lang)
                )

                DetailFactCard(
                    label = "Venue",
                    value = venue
                )

                event.ticketUrl
                    ?.takeIf(String::isNotBlank)
                    ?.let { ticketUrl ->
                        TicketActionCard(
                            url = ticketUrl,
                            onOpen = { uriHandler.openUri(ticketUrl) }
                        )
                    }
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}

@Composable
private fun OverlayCircleButton(
    symbol: String,
    onClick: (() -> Unit)? = null
) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.16f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(colors.colorWhite)
            .border(1.dp, Color.Black.copy(alpha = 0.06f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp
            ),
            color = colors.colorNavy
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    body: String
) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = colors.colorNavy.copy(alpha = 0.76f)
        )
    }
}

@Composable
private fun DetailFactCard(
    label: String,
    value: String
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.colorSurface)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy.copy(alpha = 0.56f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.colorNavy,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TicketActionCard(
    url: String,
    onOpen: () -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.colorSurface)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Tickets",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = "Open the official ticket link for availability and booking details.",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorNavy.copy(alpha = 0.78f)
        )
        Text(
            text = "Open ticket link →",
            modifier = Modifier.clickable(onClick = onOpen),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorOrangeMain
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = colors.colorSlate.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun String.prettyEventLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }

@OptIn(ExperimentalTime::class)
private fun String.toEventTimeLabel(): String? = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .let { time ->
            "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
        }
}.getOrNull()
