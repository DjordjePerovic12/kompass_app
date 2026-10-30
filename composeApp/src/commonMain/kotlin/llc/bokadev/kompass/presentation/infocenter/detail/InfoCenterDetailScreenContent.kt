package llc.bokadev.kompass.presentation.infocenter.detail

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.states_notice_source
import kompass.composeapp.generated.resources.states_notice_when
import kompass.composeapp.generated.resources.states_notice_where
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.toNoticeDate
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.presentation.shared.toNoticeLabel
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.detail.DetailHero
import llc.bokadev.kompass.presentation.shared.detail.DetailPillButton
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRow
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRows
import llc.bokadev.kompass.presentation.shared.detail.DetailTextSection
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * A local update: level and validity, what happened, where and when it applies, then the source.
 * The image only appears when the CMS has one; otherwise the header stays compact.
 * The shared detail components and notice label still take the AppStrings tables until they are migrated.
 */
@Composable
fun InfoCenterDetailScreenContent(
    modifier: Modifier = Modifier,
    state: InfoCenterDetailState,
    onBackClick: () -> Unit,
    onSourceClick: () -> Unit
) {
    val notice = state.notice ?: return
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val strings = appStringsFor(lang)
    val colors = KompassTheme.colors
    val backLabel = stringResource(Res.string.detail_back)
    val whereLabel = stringResource(Res.string.states_notice_where)
    val whenLabel = stringResource(Res.string.states_notice_when)

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailHero(
            photoUrls = listOfNotNull(notice.imageUrl?.takeIf { it.isNotBlank() }),
            height = 260.dp,
            isSaved = false,
            backLabel = backLabel,
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
            Text(text = notice.toNoticeLabel(lang, strings).uppercaseFor(lang), style = KompassTheme.typography.geologicaSemiBold11, color = colors.lampYellow)
            Text(
                text = notice.localizedTitle(lang),
                style = KompassTheme.typography.gloockRegular34.copy(fontSize = 32.sp, lineHeight = 35.sp),
                color = colors.boneWhite,
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
                notice.localizedLocation(lang).trim().takeIf { it.isNotBlank() }?.let { DetailPracticalRow(DetailGlyph.Landmark, whereLabel, it) },
                whenText?.let { DetailPracticalRow(DetailGlyph.Clock, whenLabel, it) }
            ),
            paired = emptyList(),
            lang = lang
        )
        val body = notice.localizedLongDescription(lang).trim().ifBlank { notice.localizedShortDescription(lang).trim() }
        DetailTextSection(body, strings.detail, collapsedLines = 12)
        notice.externalUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
            DetailPillButton(
                label = stringResource(Res.string.states_notice_source),
                glyph = DetailGlyph.ArrowUpRight,
                filled = false,
                height = 52.dp,
                onClick = onSourceClick,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )
        }
        Spacer(Modifier.height(26.dp))
    }
}

@Preview
@Composable
private fun InfoCenterDetailScreenContentPreview() {
    KompassTheme {
        InfoCenterDetailScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = InfoCenterDetailState(
                isLoading = false,
                notice = InfoNotice(
                    id = "preview",
                    cityId = "kotor",
                    title = mapOf("en" to "Water supply interruption"),
                    shortDescription = mapOf("en" to "No water in Dobrota between 9:00 and 14:00."),
                    longDescription = mapOf("en" to "Works on the main pipe. Tap water returns in the afternoon."),
                    priority = "important",
                    startsAt = "2026-10-12T09:00:00Z",
                    endsAt = "2026-10-12T14:00:00Z",
                    location = mapOf("en" to "Dobrota"),
                    externalUrl = "https://example.com"
                )
            ),
            onBackClick = {},
            onSourceClick = {}
        )
    }
}
