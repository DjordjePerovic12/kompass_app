package llc.bokadev.kompass.presentation.screens.infocenter

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.presentation.mvi.BaseContentView
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.core.util.toNoticeDate
import llc.bokadev.kompass.core.util.uppercaseFor
import llc.bokadev.kompass.presentation.screens.home.toNoticeLabel
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailHero
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailLoadState
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPillButton
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRows
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailTextSection
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground

/**
 * A local update: level and validity, what happened, where and when it applies, then the source.
 * The image only appears when the CMS has one; otherwise the header stays compact.
 */
@Composable
fun InfoCenterDetailScreenContent(
    state: InfoCenterDetailState,
    onAction: (InfoCenterDetailAction) -> Unit
) {
    val onRetry = { onAction(InfoCenterDetailAction.Retry) }
    val onBack = { onAction(InfoCenterDetailAction.Back) }
    StatusBarIconsEffect(lightIcons = true)
    val colors = LamplightTheme.colors
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val s = strings.states
    BaseContentView(
        state = state,
        onRetry = onRetry,
        modifier = Modifier.pageGradientBackground(),
        loading = { DetailLoadState(null, strings.retry, strings.detail.back, onRetry, onBack) },
        error = { message -> DetailLoadState(message, strings.retry, strings.detail.back, onRetry, onBack) }
    ) {
        val notice = state.notice ?: return@BaseContentView
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailHero(
                photoUrls = listOfNotNull(notice.imageUrl?.takeIf { it.isNotBlank() }),
                height = 260.dp,
                isSaved = false,
                backLabel = strings.detail.back,
                saveLabel = "",
                savedStateLabel = "",
                photoOfLabel = strings.detail.photoOf,
                onBack = onBack,
                onToggleSave = null
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = notice.toNoticeLabel(lang, strings).uppercaseFor(lang), style = LamplightType.label(), color = colors.lamp)
                Text(
                    text = notice.localizedTitle(lang),
                    style = LamplightType.cardTitleXL().copy(fontSize = 32.sp, lineHeight = 35.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            }
            val start = notice.startsAt?.toNoticeDate(lang)
            val end = notice.endsAt?.toNoticeDate(lang)
            val whenText = when {
                start != null && end != null && start != end -> "$start – $end"
                else -> start ?: end
            }
            DetailPracticalRows(
                fullWidth = listOfNotNull(
                    notice.localizedLocation(lang).trim().takeIf { it.isNotBlank() }?.let { DetailPracticalRow(DetailGlyph.Landmark, s.noticeWhere, it) },
                    whenText?.let { DetailPracticalRow(DetailGlyph.Clock, s.noticeWhen, it) }
                ),
                paired = emptyList(),
                lang = lang
            )
            val body = notice.localizedLongDescription(lang).trim().ifBlank { notice.localizedShortDescription(lang).trim() }
            DetailTextSection(body, strings.detail, collapsedLines = 12)
            notice.externalUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
                DetailPillButton(
                    label = s.noticeSource,
                    glyph = DetailGlyph.ArrowUpRight,
                    filled = false,
                    height = 52.dp,
                    onClick = { onAction(InfoCenterDetailAction.OpenSource) },
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
                )
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
