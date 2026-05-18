package llc.bokadev.kompass.presentation.screens.infocenter

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
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.theme.KompassTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

@Composable
fun InfoCenterDetailScreen(
    id: String,
    onBack: () -> Unit = {}
) {
    val vm: InfoCenterDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()

    InfoCenterDetailContent(
        state = state,
        onIntent = vm::onIntent,
        onBack = onBack
    )
}

@Composable
private fun InfoCenterDetailContent(
    state: InfoCenterDetailState,
    onIntent: (InfoCenterDetailEvent) -> Unit,
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
                        .clickable { onIntent(InfoCenterDetailEvent.Retry) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy
                )
            }

            state.notice != null -> {
                InfoCenterDetailBody(
                    notice = state.notice,
                    onBack = onBack
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoCenterDetailBody(
    notice: llc.bokadev.kompass.domain.model.InfoNotice,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val uriHandler = LocalUriHandler.current
    val priorityColor = if (notice.priorityRank() == 0) colors.colorError else colors.colorOrangeMain
    val title = notice.localizedTitle(lang)
    val metaLine = listOfNotNull(
        notice.noticeType.prettyNoticeLabel(),
        notice.localizedLocation(lang).takeIf { it.isNotBlank() }
    ).joinToString(" · ")
    val statusLine = listOfNotNull(
        notice.startsAt?.toNoticeTimeLabel(),
        notice.endsAt?.toNoticeEndLabel()
    ).joinToString(" · ")
    val chips = listOfNotNull(
        notice.priority.uppercase(),
        notice.noticeType.prettyNoticeLabel(),
        notice.localizedLocation(lang).takeIf { it.isNotBlank() }
    ).distinct().take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                PlacePhotoHeader(
                    imageUrl = notice.imageUrl,
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
                        text = title,
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
                    if (statusLine.isNotBlank()) {
                        Text(
                            text = statusLine,
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

                PriorityNoticeCard(
                    label = strings.importantTownInformation,
                    value = notice.priority.uppercase(),
                    accent = priorityColor
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                DetailSection(
                    title = "Overview",
                    body = notice.localizedShortDescription(lang)
                )

                notice.localizedLongDescription(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { longText ->
                        DetailSection(
                            title = "Details",
                            body = longText
                        )
                    }

                notice.localizedLocation(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { location ->
                        DetailFactCard(
                            label = strings.affectedArea,
                            value = location
                        )
                    }

                notice.externalUrl
                    ?.takeIf(String::isNotBlank)
                    ?.let { sourceUrl ->
                        SourceActionCard(
                            sourceUrl = sourceUrl,
                            openLabel = strings.visitSource,
                            onOpen = { uriHandler.openUri(sourceUrl) }
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
private fun PriorityNoticeCard(
    label: String,
    value: String,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = accent
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = KompassTheme.colors.colorNavy
        )
    }
}

@Composable
private fun SourceActionCard(
    sourceUrl: String,
    openLabel: String,
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
            text = "Source",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = sourceUrl,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorSlate.copy(alpha = 0.78f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$openLabel →",
            modifier = Modifier.clickable(onClick = onOpen),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorOrangeMain
        )
    }
}

private fun String.prettyNoticeLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }

@OptIn(ExperimentalTime::class)
private fun String.toNoticeTimeLabel(): String? = runCatching {
    val dateTime = kotlin.time.Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    "${dateTime.date.dayOfMonth} ${dateTime.date.month.name.take(3)} · ${
        dateTime.time.hour.toString().padStart(2, '0')
    }:${dateTime.time.minute.toString().padStart(2, '0')}"
}.getOrNull()

@OptIn(ExperimentalTime::class)
private fun String.toNoticeEndLabel(): String? = runCatching {
    val dateTime = kotlin.time.Instant.parse(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    "Until ${dateTime.time.hour.toString().padStart(2, '0')}:${dateTime.time.minute.toString().padStart(2, '0')}"
}.getOrNull()
