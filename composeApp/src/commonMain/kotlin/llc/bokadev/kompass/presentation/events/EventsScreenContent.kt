@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.presentation.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.events_ongoing
import kompass.composeapp.generated.resources.events_subtitle
import kompass.composeapp.generated.resources.events_title
import kompass.composeapp.generated.resources.events_today
import kompass.composeapp.generated.resources.events_tomorrow
import kompass.composeapp.generated.resources.no_events_for_filters
import kompass.composeapp.generated.resources.no_events_yet
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.states_couldnt_refresh
import kompass.composeapp.generated.resources.states_filter_type
import kompass.composeapp.generated.resources.states_filter_when
import kompass.composeapp.generated.resources.states_reset_filters
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.toClockTime
import llc.bokadev.kompass.core.utils.toDayLabel
import llc.bokadev.kompass.domain.model.Destination
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.model.EventFilterKind
import llc.bokadev.kompass.domain.model.groupedByListingDay
import llc.bokadev.kompass.domain.model.isOngoingFromEarlierDay
import llc.bokadev.kompass.presentation.events.components.EventRow
import llc.bokadev.kompass.presentation.shared.LamplightChip
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Events in chronological Kotor-day groups (Today, Tomorrow, then dates). A festival that started
 * earlier and is still on sits under Today as "Ongoing". Filters stay usable while loading.
 */
@Composable
fun EventsScreenContent(
    modifier: Modifier = Modifier,
    state: EventsState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDateFilterClick: (String) -> Unit,
    onTypeFilterClick: (String) -> Unit,
    onResetFiltersClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val now = Clock.System.now()
    val today = now.toLocalDateTime(Destination.timeZone).date
    val groups = state.events.groupedByListingDay(now)
    val todayLabel = stringResource(Res.string.events_today)
    val tomorrowLabel = stringResource(Res.string.events_tomorrow)
    val ongoingLabel = stringResource(Res.string.events_ongoing)
    val retryLabel = stringResource(Res.string.retry)
    val error = state.error

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colors.nightBlack),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = stringResource(Res.string.events_title),
                subtitle = stringResource(Res.string.events_subtitle),
                backLabel = stringResource(Res.string.detail_back),
                onBack = onBackClick
            )
        }
        if (state.dateFilters.isNotEmpty()) {
            item(key = "date-filters") { FilterRow(stringResource(Res.string.states_filter_when), state.dateFilters, state.selectedDateFilter, lang, onDateFilterClick) }
        }
        if (state.typeFilters.isNotEmpty()) {
            item(key = "type-filters") { FilterRow(stringResource(Res.string.states_filter_type), state.typeFilters, state.selectedTypeFilter, lang, onTypeFilterClick) }
        }
        if (state.isLoading || state.isRefreshing) {
            item(key = "progress") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = colors.lampYellow, trackColor = colors.slateDark)
            }
        }

        val pad = Modifier.padding(horizontal = 20.dp)
        when {
            error != null -> item(key = "failed") {
                StateMessage(stringResource(error.messageRes), pad, actionLabel = retryLabel, onAction = onRetryClick)
            }
            state.events.isEmpty() && !state.isLoading && !state.isRefreshing -> item(key = "empty") {
                if (state.isFiltered) StateMessage(stringResource(Res.string.no_events_for_filters), pad, actionLabel = stringResource(Res.string.states_reset_filters), onAction = onResetFiltersClick)
                else StateMessage(stringResource(Res.string.no_events_yet), pad)
            }
            else -> {
                if (state.refreshFailed) {
                    item(key = "stale") { StateMessage(stringResource(Res.string.states_couldnt_refresh), pad, actionLabel = retryLabel, onAction = onRetryClick) }
                }
                groups.forEach { (day, events) ->
                    item(key = "day-$day") { DayHeader(day.label(today, lang, todayLabel, tomorrowLabel)) }
                    items(events, key = { it.id }) { event ->
                        EventRow(
                            time = if (event.isOngoingFromEarlierDay(now)) ongoingLabel else event.startTime.toClockTime().orEmpty(),
                            title = event.localizedName(lang),
                            venue = event.localizedVenue(lang).takeIf { it.isNotBlank() },
                            price = event.price?.takeIf { it.isNotBlank() },
                            onClick = { onEventClick(event.id) },
                            modifier = pad
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(title: String, filters: List<EventFilter>, selectedKey: String, lang: String, onSelect: (String) -> Unit) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = KompassTheme.typography.geologicaSemiBold11, color = colors.pebbleGray, modifier = Modifier.padding(start = 20.dp, top = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters, key = { it.key }) { filter ->
                LamplightChip(label = filter.localizedLabel(lang), selected = filter.key == selectedKey, onClick = { onSelect(filter.key) })
            }
        }
    }
}

@Composable
private fun DayHeader(text: String) {
    Text(
        text = text,
        style = KompassTheme.typography.gloockRegular26,
        color = KompassTheme.colors.boneWhite,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp).semantics { heading() }
    )
}

private fun LocalDate.label(today: LocalDate, lang: String, todayLabel: String, tomorrowLabel: String): String = when (this) {
    today -> todayLabel
    today.plus(DatePeriod(days = 1)) -> tomorrowLabel
    else -> toDayLabel(lang)
}

@Preview
@Composable
private fun EventsScreenContentPreview() {
    KompassTheme {
        EventsScreenContent(
            state = EventsState(
                dateFilters = listOf(
                    EventFilter(id = "all_dates", key = ALL_DATES, kind = EventFilterKind.DATE, label = mapOf("en" to "All dates"), value = "all", sortOrder = 0, isActive = true),
                    EventFilter(id = "today", key = "today", kind = EventFilterKind.DATE, label = mapOf("en" to "Today"), value = "today", sortOrder = 1, isActive = true)
                ),
                events = listOf(
                    Event(
                        id = "preview",
                        name = mapOf("en" to "Kotor Summer Fest: Klapa evening"),
                        description = emptyMap(),
                        venue = mapOf("en" to "Square of Arms"),
                        latitude = 42.42,
                        longitude = 18.77,
                        category = "music",
                        startTime = "2026-10-08T18:00:00Z",
                        endTime = null,
                        price = "Free",
                        ticketUrl = null,
                        isRecurring = false,
                        photos = emptyList()
                    )
                )
            ),
            onBackClick = {},
            onRetryClick = {},
            onDateFilterClick = {},
            onTypeFilterClick = {},
            onResetFiltersClick = {},
            onEventClick = {}
        )
    }
}
