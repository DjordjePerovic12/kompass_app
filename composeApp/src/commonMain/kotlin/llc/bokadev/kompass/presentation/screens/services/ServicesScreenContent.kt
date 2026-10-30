package llc.bokadev.kompass.presentation.screens.services

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
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.presentation.screens.essentials.linkTitle
import llc.bokadev.kompass.presentation.screens.services.components.ServiceRow
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.presentation.theme.LamplightTheme

/** Services as compact functional rows; no trust/official labels without a validation process behind them. */
@Composable
fun ServicesScreenContent(
    state: ServicesState,
    onAction: (ServicesAction) -> Unit
) {
    val colors = LamplightTheme.colors
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val s = strings.states
    val pad = Modifier.padding(horizontal = 20.dp)
    val retry = { onAction(ServicesAction.Retry) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.night),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = strings.servicesTitle,
                subtitle = strings.servicesSubtitle,
                backLabel = strings.detail.back,
                onBack = { onAction(ServicesAction.Back) }
            )
        }
        if (state.isLoading) {
            item(key = "progress") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), color = colors.lamp, trackColor = colors.surfaceMuted)
            }
        }
        when {
            state.error != null -> item(key = "failed") {
                StateMessage(state.error.asString(), pad, actionLabel = strings.retry, onAction = retry)
            }
            state.services.isEmpty() && !state.isLoading -> item(key = "empty") { StateMessage(s.noServices, pad) }
            state.refreshFailed -> item(key = "stale") { StateMessage(s.couldntRefresh, pad, actionLabel = strings.retry, onAction = retry) }
        }
        items(state.services, key = { it.id }) { service ->
            ServiceRow(
                name = service.localizedName(lang),
                area = service.localizedLocation(lang)?.trim()?.takeIf { it.isNotBlank() },
                contact = service.externalWebsite?.trim()?.takeIf { it.isNotBlank() }?.linkTitle(),
                onClick = { onAction(ServicesAction.OpenService(service.id)) },
                modifier = pad
            )
        }
    }
}
