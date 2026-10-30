package llc.bokadev.kompass.presentation.infocenter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.info_center_subtitle
import kompass.composeapp.generated.resources.info_center_title
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.states_couldnt_refresh
import kompass.composeapp.generated.resources.states_no_updates
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.presentation.screens.home.components.NoticeItem
import llc.bokadev.kompass.presentation.screens.home.toNoticeLabel
import llc.bokadev.kompass.presentation.screens.home.toNoticeLevel
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Local updates: what happened and its level/validity first ("Important · until 12 Oct"), where it
 * applies, then the short summary. Rows are text-first; imagery belongs on the detail.
 * The shared notice label still takes the AppStrings tables until it is migrated.
 */
@Composable
fun InfoCenterScreenContent(
    modifier: Modifier = Modifier,
    state: InfoCenterState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNoticeClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val strings = appStringsFor(lang)
    val retryLabel = stringResource(Res.string.retry)
    val pad = Modifier.padding(horizontal = 20.dp)
    val error = state.error

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colors.nightBlack),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = stringResource(Res.string.info_center_title),
                subtitle = stringResource(Res.string.info_center_subtitle),
                backLabel = stringResource(Res.string.detail_back),
                onBack = onBackClick
            )
        }
        if (state.isLoading) {
            item(key = "progress") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), color = colors.lampYellow, trackColor = colors.slateDark)
            }
        }
        when {
            error != null -> item(key = "failed") {
                StateMessage(stringResource(error.messageRes), pad, actionLabel = retryLabel, onAction = onRetryClick)
            }
            state.notices.isEmpty() && !state.isLoading -> item(key = "empty") { StateMessage(stringResource(Res.string.states_no_updates), pad) }
            state.refreshFailed -> item(key = "stale") { StateMessage(stringResource(Res.string.states_couldnt_refresh), pad, actionLabel = retryLabel, onAction = onRetryClick) }
        }
        items(state.notices, key = { it.id }) { notice ->
            val where = notice.localizedLocation(lang).trim().takeIf { it.isNotBlank() }
            NoticeItem(
                level = notice.toNoticeLevel(),
                label = listOfNotNull(notice.toNoticeLabel(lang, strings), where).joinToString(" · "),
                title = notice.localizedTitle(lang),
                // A summary that only repeats the title adds nothing.
                summary = notice.localizedShortDescription(lang).takeUnless { it.trim().equals(notice.localizedTitle(lang).trim(), ignoreCase = true) }.orEmpty(),
                onClick = { onNoticeClick(notice.id) },
                modifier = pad
            )
        }
    }
}

@Preview
@Composable
private fun InfoCenterScreenContentPreview() {
    KompassTheme {
        InfoCenterScreenContent(
            state = InfoCenterState(
                isLoading = false,
                notices = listOf(
                    InfoNotice(
                        id = "preview",
                        cityId = "kotor",
                        title = mapOf("en" to "Water supply interruption"),
                        shortDescription = mapOf("en" to "No water in Dobrota between 9:00 and 14:00."),
                        priority = "important",
                        location = mapOf("en" to "Dobrota")
                    )
                )
            ),
            onBackClick = {},
            onRetryClick = {},
            onNoticeClick = {}
        )
    }
}
