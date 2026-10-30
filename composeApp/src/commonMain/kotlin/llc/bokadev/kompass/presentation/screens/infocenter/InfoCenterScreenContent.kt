package llc.bokadev.kompass.presentation.screens.infocenter

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
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.mvi.asString
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.screens.home.components.NoticeItem
import llc.bokadev.kompass.presentation.screens.home.toNoticeLabel
import llc.bokadev.kompass.presentation.screens.home.toNoticeLevel
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.presentation.theme.LamplightTheme

/**
 * Local updates: what happened and its level/validity first ("Important · until 12 Oct"), where it
 * applies, then the short summary. Rows are text-first; imagery belongs on the detail.
 */
@Composable
fun InfoCenterScreenContent(
    state: InfoCenterState,
    onAction: (InfoCenterAction) -> Unit
) {
    val onRetry = { onAction(InfoCenterAction.Retry) }
    val colors = LamplightTheme.colors
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val s = strings.states
    val pad = Modifier.padding(horizontal = 20.dp)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.night),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = strings.infoCenterTitle,
                subtitle = strings.infoCenterSubtitle,
                backLabel = strings.detail.back,
                onBack = { onAction(InfoCenterAction.Back) }
            )
        }
        if (state.isLoading) {
            item(key = "progress") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), color = colors.lamp, trackColor = colors.surfaceMuted)
            }
        }
        when {
            state.error != null -> item(key = "failed") {
                StateMessage(state.error.asString(), pad, actionLabel = strings.retry, onAction = onRetry)
            }
            state.notices.isEmpty() && !state.isLoading -> item(key = "empty") { StateMessage(s.noUpdates, pad) }
            state.refreshFailed -> item(key = "stale") { StateMessage(s.couldntRefresh, pad, actionLabel = strings.retry, onAction = onRetry) }
        }
        items(state.notices, key = { it.id }) { notice ->
            val where = notice.localizedLocation(lang).trim().takeIf { it.isNotBlank() }
            NoticeItem(
                level = notice.toNoticeLevel(),
                label = listOfNotNull(notice.toNoticeLabel(lang, strings), where).joinToString(" · "),
                title = notice.localizedTitle(lang),
                // A summary that only repeats the title adds nothing.
                summary = notice.localizedShortDescription(lang).takeUnless { it.trim().equals(notice.localizedTitle(lang).trim(), ignoreCase = true) }.orEmpty(),
                onClick = { onAction(InfoCenterAction.OpenNotice(notice.id)) },
                modifier = pad
            )
        }
    }
}
