@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.presentation.eventdetail

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.detail_open_in_maps
import kompass.composeapp.generated.resources.detail_where_it_is
import kompass.composeapp.generated.resources.events_price
import kompass.composeapp.generated.resources.events_tickets
import kompass.composeapp.generated.resources.events_time
import kompass.composeapp.generated.resources.events_until
import kompass.composeapp.generated.resources.events_venue
import kotlinx.datetime.toLocalDateTime
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.toDayLabel
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.Destination
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.window
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailHero
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPillButton
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRows
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailTextSection
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailWhereItIs
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

/**
 * Event detail in the shared detail language: when, where and price first, then the official
 * ticket link, then the description and map. Times are Kotor time; sections show only with data.
 * The shared detail components still take the AppStrings tables until they are migrated.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    state: EventDetailState,
    currentLocation: GeoPoint?,
    onBackClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onMapsClick: () -> Unit
) {
    val event = state.event ?: return
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val strings = appStringsFor(lang)
    val colors = KompassTheme.colors
    val density = LocalDensity.current
    val name = event.localizedName(lang)
    val venue = event.localizedVenue(lang).trim().takeIf { it.isNotBlank() }
    val timeLabel = stringResource(Res.string.events_time)
    val venueLabel = stringResource(Res.string.events_venue)
    val priceLabel = stringResource(Res.string.events_price)
    val untilTemplate = stringResource(Res.string.events_until, "{date}")
    val screenHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailHero(
            photoUrls = event.photos.map(::buildPhotoUrl),
            height = (screenHeight * 0.36f).coerceIn(240.dp, 380.dp),
            isSaved = false,
            backLabel = stringResource(Res.string.detail_back),
            saveLabel = "",
            savedStateLabel = "",
            photoOfLabel = strings.detail.photoOf,
            onBack = onBackClick,
            onToggleSave = null
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            event.category.trim().takeIf { it.isNotBlank() }?.let {
                Text(text = it.toTitleCaseLabel().uppercaseFor(lang), style = KompassTheme.typography.geologicaSemiBold11, color = colors.lampYellow)
            }
            Text(
                text = name,
                style = KompassTheme.typography.gloockRegular34.copy(fontSize = 36.sp, lineHeight = 38.sp),
                color = colors.boneWhite,
                modifier = Modifier.semantics { heading() }
            )
        }
        DetailPracticalRows(
            fullWidth = listOfNotNull(
                event.whenLabel(lang, untilTemplate)?.let { DetailPracticalRow(DetailGlyph.Clock, timeLabel, it) },
                venue?.let { DetailPracticalRow(DetailGlyph.Landmark, venueLabel, it) },
                event.price?.trim()?.takeIf { it.isNotBlank() }?.let { DetailPracticalRow(DetailGlyph.Price, priceLabel, it) }
            ),
            paired = emptyList(),
            lang = lang
        )
        event.ticketUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
            DetailPillButton(
                label = stringResource(Res.string.events_tickets),
                glyph = DetailGlyph.ArrowUpRight,
                filled = true,
                height = 56.dp,
                onClick = onTicketsClick,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )
        }
        DetailTextSection(event.localizedDescription(lang).trim(), strings.detail)
        if (event.latitude != 0.0 || event.longitude != 0.0) {
            DetailWhereItIs(
                title = stringResource(Res.string.detail_where_it_is),
                name = venue ?: name,
                destination = GeoPoint(event.latitude, event.longitude),
                currentLocation = currentLocation,
                areaLabel = venue,
                openInMapsLabel = stringResource(Res.string.detail_open_in_maps),
                onOpenMaps = onMapsClick
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

@Preview
@Composable
private fun EventDetailScreenContentPreview() {
    KompassTheme {
        EventDetailScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = EventDetailState(
                isLoading = false,
                event = Event(
                    id = "preview",
                    name = mapOf("en" to "Cetinje Outdoor Festival"),
                    description = mapOf("en" to "Challenge yourself in the Montenegrin national parks."),
                    venue = mapOf("en" to "Ivanova Korita"),
                    latitude = 42.39,
                    longitude = 18.91,
                    category = "sport",
                    startTime = "2026-10-05T11:00:00Z",
                    endTime = "2026-11-25T14:00:00Z",
                    price = "30 € - 50 €",
                    ticketUrl = "https://example.com",
                    isRecurring = false,
                    photos = emptyList()
                )
            ),
            currentLocation = null,
            onBackClick = {},
            onTicketsClick = {},
            onMapsClick = {}
        )
    }
}
