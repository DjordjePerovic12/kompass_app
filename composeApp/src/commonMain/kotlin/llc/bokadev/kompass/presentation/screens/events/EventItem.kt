@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.presentation.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.presentation.theme.KompassTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@Composable
fun EventItem(
    modifier: Modifier = Modifier,
    event: Event,
    lang: String = "en"
) {
    val eventDay = event.startTime.toEventDay()
    val eventMonth = event.startTime.toEventMonth()
    val eventName = event.localizedName(lang)
    val eventVenue = event.localizedVenue(lang)
    val eventMeta = event.toEventMeta()
    val cardShape = RoundedCornerShape(18.dp)
    val dateShape = RoundedCornerShape(8.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Start),
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                shape = cardShape,
                color = KompassTheme.colors.colorSlateGhost
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(dateShape)
                .background(KompassTheme.colors.dateBoxBackground)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 18.dp)
            ) {
                Text(
                    text = eventDay,
                    color = KompassTheme.colors.eventDateText
                )
                Text(
                    text = eventMonth,
                    color = KompassTheme.colors.eventDateText
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp, alignment = Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = eventName,
                color = KompassTheme.colors.colorNavy
            )

            Text(
                text = eventVenue,
                color = KompassTheme.colors.colorSlate
            )

            if (eventMeta.isNotBlank()) {
                Text(
                    text = eventMeta,
                    color = KompassTheme.colors.colorSlate
                )
            }
        }
    }
}

private fun String.toEventDay(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .day
        .toString()
}.getOrDefault("--")

private fun String.toEventMonth(): String = runCatching {
    Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .month
        .name
        .take(3)
}.getOrDefault("---")

private fun Event.toEventMeta(): String {
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
