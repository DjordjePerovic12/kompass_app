@file:OptIn(kotlin.time.ExperimentalTime::class)

package llc.bokadev.kompass.presentation.screens.events

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.presentation.screens.home.components.EventCard
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun EventItem(
    modifier: Modifier = Modifier,
    event: Event,
    lang: String = "en",
    onClick: () -> Unit = {}
) {
    EventCard(
        modifier = modifier.padding(horizontal = 20.dp),
        name = event.localizedName(lang),
        venue = event.localizedVenue(lang),
        day = event.startTime.toEventDay(),
        month = event.startTime.toEventMonth(),
        meta = event.toEventMeta(),
        price = event.price,
        onClick = onClick
    )
}

fun String.toEventDay(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .day
        .toString()
}.getOrDefault("--")

fun String.toEventMonth(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .month
        .name
        .take(3)
}.getOrDefault("---")

fun Event.toEventMeta(): String {
    val start = startTime.toEventTime()
    val end = endTime?.toEventTime()
    val timeRange = when {
        start != null && end != null -> "$start – $end"
        start != null -> start
        else -> ""
    }

    return listOfNotNull(
        timeRange.takeIf { it.isNotBlank() },
        price?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")
}

private fun String.toEventTime(): String? = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
        .let { time ->
            "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
        }
}.getOrNull()
