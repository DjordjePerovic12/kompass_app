package llc.bokadev.kompass.presentation.services

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
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.services_subtitle
import kompass.composeapp.generated.resources.services_title
import kompass.composeapp.generated.resources.states_couldnt_refresh
import kompass.composeapp.generated.resources.states_no_services
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.screens.essentials.linkTitle
import llc.bokadev.kompass.presentation.services.components.ServiceRow
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.StateMessage
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource

/** Services as compact functional rows; no trust/official labels without a validation process behind them. */
@Composable
fun ServicesScreenContent(
    modifier: Modifier = Modifier,
    state: ServicesState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onServiceClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
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
                title = stringResource(Res.string.services_title),
                subtitle = stringResource(Res.string.services_subtitle),
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
            state.services.isEmpty() && !state.isLoading -> item(key = "empty") { StateMessage(stringResource(Res.string.states_no_services), pad) }
            state.refreshFailed -> item(key = "stale") { StateMessage(stringResource(Res.string.states_couldnt_refresh), pad, actionLabel = retryLabel, onAction = onRetryClick) }
        }
        items(state.services, key = { it.id }) { service ->
            ServiceRow(
                name = service.localizedName(lang),
                area = service.localizedLocation(lang)?.trim()?.takeIf { it.isNotBlank() },
                contact = service.externalWebsite?.trim()?.takeIf { it.isNotBlank() }?.linkTitle(),
                onClick = { onServiceClick(service.id) },
                modifier = pad
            )
        }
    }
}

@Preview
@Composable
private fun ServicesScreenContentPreview() {
    KompassTheme {
        ServicesScreenContent(
            state = ServicesState(
                isLoading = false,
                services = listOf(
                    Service(
                        id = "preview",
                        name = mapOf("en" to "Bay Laundry"),
                        description = emptyMap(),
                        location = mapOf("en" to "Dobrota"),
                        externalWebsite = "https://baylaundry.me"
                    )
                )
            ),
            onBackClick = {},
            onRetryClick = {},
            onServiceClick = {}
        )
    }
}
