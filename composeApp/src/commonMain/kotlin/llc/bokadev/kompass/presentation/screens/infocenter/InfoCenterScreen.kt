package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
                slug = strings.infoCenterSlug,
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
                .background(KompassTheme.colors.colorSurface),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.notices, key = { it.id }) { notice ->
                Column(
                    modifier = Modifier
                    .fillMaxWidth()
                        .background(KompassTheme.colors.colorWhite, RoundedCornerShape(18.dp))
                        .clickable {
                            analytics.trackInfoNoticeView(
                                noticeId = notice.id,
                                cityId = notice.cityId,
                                contentOrigin = "info_center"
                            )
                            onNoticeClick(notice.id)
                        }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = notice.priority.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (notice.priorityRank() == 0) {
                            KompassTheme.colors.colorError
                        } else {
                            KompassTheme.colors.colorAmberDark
                        }
                    )
                    Text(
                        text = notice.localizedTitle(lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = KompassTheme.colors.colorNavy
                    )
                    Text(
                        text = notice.localizedShortDescription(lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = KompassTheme.colors.colorSlate
                    )
                    val meta = listOfNotNull(
                        notice.noticeType.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        notice.localizedLocation(lang).ifBlank { null }
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = KompassTheme.colors.colorSlateLight
                        )
                    }
                }
            }
        }
    }
}
