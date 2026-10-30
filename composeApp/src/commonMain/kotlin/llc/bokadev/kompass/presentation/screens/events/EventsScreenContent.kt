@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.presentation.screens.events

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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import llc.bokadev.kompass.core.presentation.mvi.asString
import llc.bokadev.kompass.core.util.AppStrings
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.core.util.toClockTime
import llc.bokadev.kompass.core.util.toDayLabel
import llc.bokadev.kompass.domain.model.Destination
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.model.groupedByListingDay
import llc.bokadev.kompass.domain.model.isOngoingFromEarlierDay
import llc.bokadev.kompass.presentation.screens.events.components.EventRow
import llc.bokadev.kompass.presentation.shared.LamplightChip
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Events in chronological Kotor-day groups (Today, Tomorrow, then dates). A festival that started
 * earlier and is still on sits under Today as "Ongoing". Filters stay usable while loading.
 */
@Composable
fun EventsScreenContent(state: EventsState, onAction: (EventsAction) -> Unit) {
    val colors = LamplightTheme.colors
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val s = strings.states
    val now = Clock.System.now()
    val today = now.toLocalDateTime(Destination.timeZone).date
    val groups = state.events.groupedByListingDay(now)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.night),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = strings.eventsTitle,
                subtitle = strings.eventsSubtitle,
                backLabel = strings.detail.back,
                onBack = { onAction(EventsAction.Back) }
            )
        }
        if (state.dateFilters.isNotEmpty()) {
            item(key = "date-filters") { FilterRow(s.filterWhen, state.dateFilters, state.selectedDateFilter, lang) { onAction(EventsAction.SelectDateFilter(it)) } }
        }
        if (state.typeFilters.isNotEmpty()) {
            item(key = "type-filters") { FilterRow(s.filterType, state.typeFilters, state.selectedTypeFilter, lang) { onAction(EventsAction.SelectTypeFilter(it)) } }
        }
        if (state.isLoading || state.isRefreshing) {
            item(key = "progress") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = colors.lamp, trackColor = colors.surfaceMuted)
            }
        }

        val pad = Modifier.padding(horizontal = 20.dp)
        when {
            state.error != null -> item(key = "failed") {
                StateMessage(state.error.asString(), pad, actionLabel = strings.retry, onAction = { onAction(EventsAction.Retry) })
            }
            state.events.isEmpty() && !state.isLoading && !state.isRefreshing -> item(key = "empty") {
                if (state.isFiltered) StateMessage(strings.noEventsForFilters, pad, actionLabel = s.resetFilters, onAction = { onAction(EventsAction.ResetFilters) })
                else StateMessage(strings.noEventsYet, pad)
            }
            else -> {
                if (state.refreshFailed) {
                    item(key = "stale") { StateMessage(s.couldntRefresh, pad, actionLabel = strings.retry, onAction = { onAction(EventsAction.Retry) }) }
                }
                groups.forEach { (day, events) ->
                    item(key = "day-$day") { DayHeader(day.label(today, lang, strings)) }
                    items(events, key = { it.id }) { event ->
                        EventRow(
                            time = if (event.isOngoingFromEarlierDay(now)) strings.events.ongoing else event.startTime.toClockTime().orEmpty(),
                            title = event.localizedName(lang),
                            venue = event.localizedVenue(lang).takeIf { it.isNotBlank() },
                            price = event.price?.takeIf { it.isNotBlank() },
                            onClick = { onAction(EventsAction.OpenEvent(event.id)) },
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
    val colors = LamplightTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = LamplightType.label(), color = colors.textTertiary, modifier = Modifier.padding(start = 20.dp, top = 8.dp))
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
        style = LamplightType.sectionTitleS(),
        color = LamplightTheme.colors.textPrimary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp).semantics { heading() }
    )
}

private fun LocalDate.label(today: LocalDate, lang: String, strings: AppStrings): String = when (this) {
    today -> strings.events.today
    today.plus(DatePeriod(days = 1)) -> strings.events.tomorrow
    else -> toDayLabel(lang)
}
