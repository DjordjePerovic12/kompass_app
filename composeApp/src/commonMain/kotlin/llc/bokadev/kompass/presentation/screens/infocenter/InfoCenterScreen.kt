package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import androidx.compose.foundation.background
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun InfoCenterScreen(
    vmKey: String = "info-center",
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    onNoticeClick: (String) -> Unit = {}
) {
    val vm: InfoCenterViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("info_center")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "",
                title = strings.infoCenterTitle,
                subtitle = strings.infoCenterSubtitle,
                showBack = showBack,
                onBackClick = onBack
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KompassTheme.colors.colorWhite),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(state.notices, key = { it.id }) { notice ->
                InfoNoticeListCard(
                    notice = notice,
                    lang = lang,
                    onClick = {
                        analytics.trackInfoNoticeView(
                            noticeId = notice.id,
                            cityId = notice.cityId,
                            contentOrigin = "info_center"
                        )
                        onNoticeClick(notice.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoNoticeListCard(
    notice: InfoNotice,
    lang: String,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val meta = listOfNotNull(
        notice.noticeType.prettyNoticeLabel(),
        notice.startsAt?.toNoticeMetaTime(),
        notice.localizedLocation(lang).takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(
                10.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(if (notice.priorityRank() == 0) colors.colorOrangeMain else colors.colorNavy)
            .clickable(onClick = onClick)
    ) {
        if (!notice.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = notice.imageUrl,
                contentDescription = notice.localizedTitle(lang),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0.7f) }
                ),
                alpha = 0.92f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.22f),
                            Color.Black.copy(alpha = 0.82f)
                        ),
                        startY = 36f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.colorOrangeMain.copy(alpha = 0.08f),
                            colors.colorOrangeMain.copy(alpha = 0.20f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomStart)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = notice.noticeType.prettyNoticeLabel(),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = colors.colorWhite.copy(alpha = 0.9f)
            )
            Text(
                text = notice.localizedTitle(lang),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, lineHeight = 28.sp),
                color = colors.colorWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = notice.localizedShortDescription(lang),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                color = colors.colorWhite.copy(alpha = 0.86f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = colors.colorWhite.copy(alpha = 0.72f),
                    maxLines = 1
                )
            }
        }
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
private fun String.toNoticeMetaTime(): String =
    runCatching {
        val local = Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
        val day = local.date.day
        val month = local.month.name.lowercase().replaceFirstChar { it.titlecase() }.take(3)
        "$day $month"
    }.getOrElse { this }
