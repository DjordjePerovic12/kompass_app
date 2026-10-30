@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.presentation.screens.eventdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.toLocalDateTime
import llc.bokadev.kompass.core.presentation.mvi.BaseContentView
import llc.bokadev.kompass.core.util.AppStrings
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.core.util.toDayLabel
import llc.bokadev.kompass.core.util.toTitleCaseLabel
import llc.bokadev.kompass.core.util.uppercaseFor
import llc.bokadev.kompass.domain.model.Destination
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.window
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailHero
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailLoadState
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPillButton
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRows
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailTextSection
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailWhereItIs
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground
import kotlin.time.ExperimentalTime

/**
 * Event detail in the shared detail language: when, where and price first, then the official
 * ticket link, then the description and map. Times are Kotor time; sections show only with data.
 */
@Composable
fun EventDetailScreenContent(state: EventDetailState, currentLocation: GeoPoint?, onAction: (EventDetailAction) -> Unit) {
    StatusBarIconsEffect(lightIcons = true)
    val strings = rememberAppStrings()
    val retry = { onAction(EventDetailAction.Retry) }
    val back = { onAction(EventDetailAction.Back) }
    BaseContentView(
        state = state,
        onRetry = retry,
        modifier = Modifier.pageGradientBackground(),
        loading = { DetailLoadState(null, strings.retry, strings.detail.back, retry, back) },
        error = { message -> DetailLoadState(message, strings.retry, strings.detail.back, retry, back) }
    ) {
        state.event?.let { Body(it, currentLocation, strings, onAction) }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Body(event: Event, currentLocation: GeoPoint?, strings: AppStrings, onAction: (EventDetailAction) -> Unit) {
    val colors = LamplightTheme.colors
    val lang = currentAppLanguage()
    val density = LocalDensity.current
    val name = event.localizedName(lang)
    val venue = event.localizedVenue(lang).trim().takeIf { it.isNotBlank() }
    val e = strings.events
    val screenHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailHero(
            photoUrls = event.photos.map(::buildPhotoUrl),
            height = (screenHeight * 0.36f).coerceIn(240.dp, 380.dp),
            isSaved = false,
            backLabel = strings.detail.back,
            saveLabel = "",
            savedStateLabel = "",
            photoOfLabel = strings.detail.photoOf,
            onBack = { onAction(EventDetailAction.Back) },
            onToggleSave = null
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            event.category.trim().takeIf { it.isNotBlank() }?.let {
                Text(text = it.toTitleCaseLabel().uppercaseFor(lang), style = LamplightType.label(), color = colors.lamp)
            }
            Text(
                text = name,
                style = LamplightType.cardTitleXL().copy(fontSize = 36.sp, lineHeight = 38.sp),
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
        }
        DetailPracticalRows(
            fullWidth = listOfNotNull(
                event.whenLabel(lang, e.until)?.let { DetailPracticalRow(DetailGlyph.Clock, e.time, it) },
                venue?.let { DetailPracticalRow(DetailGlyph.Landmark, e.venue, it) },
                event.price?.trim()?.takeIf { it.isNotBlank() }?.let { DetailPracticalRow(DetailGlyph.Price, e.price, it) }
            ),
            paired = emptyList(),
            lang = lang
        )
        event.ticketUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
            DetailPillButton(
                label = e.tickets,
                glyph = DetailGlyph.ArrowUpRight,
                filled = true,
                height = 56.dp,
                onClick = { onAction(EventDetailAction.OpenTickets) },
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )
        }
        DetailTextSection(event.localizedDescription(lang).trim(), strings.detail)
        if (event.latitude != 0.0 || event.longitude != 0.0) {
            DetailWhereItIs(
                title = strings.detail.whereItIs,
                name = venue ?: name,
                destination = GeoPoint(event.latitude, event.longitude),
                currentLocation = currentLocation,
                areaLabel = venue,
                openInMapsLabel = strings.detail.openInMaps,
                onOpenMaps = { onAction(EventDetailAction.OpenMaps) }
            )
        }
        Spacer(Modifier.height(26.dp))
    }
}

/**
 * "8 Oct, 20:00 – 22:00", or "8 Oct, 20:00 · Until 11 Oct" for multi-day events (Kotor time).
 * No end time is invented when the CMS has none.
 */
internal fun Event.whenLabel(lang: String, untilTemplate: String): String? {
    val window = window() ?: return null
    val zone = Destination.timeZone
    val start = window.start.toLocalDateTime(zone)
    val end = window.end?.toLocalDateTime(zone)
    val startText = "${start.date.toDayLabel(lang)}, ${start.time.clock()}"
    return when {
        end == null -> startText
        end.date == start.date -> "$startText – ${end.time.clock()}"
        else -> "$startText · ${untilTemplate.replace("{date}", end.date.toDayLabel(lang))}"
    }
}

private fun kotlinx.datetime.LocalTime.clock() = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

