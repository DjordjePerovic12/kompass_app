package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun InfoCenterDetailScreen(
    id: String,
    onBack: () -> Unit = {}
) {
    val vm: InfoCenterDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val uriHandler = LocalUriHandler.current

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = strings.importantTownInformation,
                title = state.notice?.localizedTitle(lang) ?: strings.infoCenterTitle,
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        val notice = state.notice ?: return@BaseContentView
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KompassTheme.colors.colorSurface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = notice.priority.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (notice.priorityRank() == 0) {
                    KompassTheme.colors.colorError
                } else {
                    KompassTheme.colors.colorAmberDark
                }
            )
            Text(
                text = notice.localizedShortDescription(lang),
                style = MaterialTheme.typography.bodyLarge,
                color = KompassTheme.colors.colorSlate
            )
            if (notice.localizedLongDescription(lang).isNotBlank()) {
                Text(
                    text = notice.localizedLongDescription(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = KompassTheme.colors.colorNavy
                )
            }
            if (notice.localizedLocation(lang).isNotBlank()) {
                Text(
                    text = "${strings.affectedArea}: ${notice.localizedLocation(lang)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KompassTheme.colors.colorSlate
                )
            }
            if (notice.externalUrl != null) {
                Text(
                    text = strings.openSourceLink,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KompassTheme.colors.colorAmberDark,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = notice.externalUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = KompassTheme.colors.colorAmberDark,
                    modifier = Modifier.padding(top = 2.dp)
                )
                androidx.compose.foundation.text.ClickableText(
                    text = androidx.compose.ui.text.AnnotatedString(strings.visitSource),
                    style = MaterialTheme.typography.bodyMedium.copy(color = KompassTheme.colors.colorAmberDark),
                    onClick = { uriHandler.openUri(notice.externalUrl) }
                )
            }
        }
    }
}
