package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.foundation.background
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
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.screens.essentials.linkTitle
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailLoadState
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPillButton
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRows
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailTextSection
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailWhereItIs
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** A service: area and contact first, then what it offers; the website/map only when valid data exists. */
@Composable
fun ServiceDetailScreenContent(
    state: ServiceDetailState,
    currentLocation: GeoPoint?,
    onAction: (ServiceDetailAction) -> Unit
) {
    val onRetry = { onAction(ServiceDetailAction.Retry) }
    val onBack = { onAction(ServiceDetailAction.Back) }
    val colors = LamplightTheme.colors
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val s = strings.states
    BaseContentView(
        state = state,
        onRetry = onRetry,
        modifier = Modifier.background(colors.night),
        loading = { DetailLoadState(null, strings.retry, strings.detail.back, onRetry, onBack) },
        error = { message -> DetailLoadState(message, strings.retry, strings.detail.back, onRetry, onBack) }
    ) {
        val service = state.service ?: return@BaseContentView
        val name = service.localizedName(lang)
        val area = service.localizedLocation(lang)?.trim()?.takeIf { it.isNotBlank() }
        val website = service.externalWebsite?.trim()?.takeIf { it.isNotBlank() }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            LamplightCompactHeader(title = strings.servicesTitle, subtitle = null, backLabel = strings.detail.back, onBack = onBack)
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = name,
                    style = LamplightType.cardTitleXL().copy(fontSize = 32.sp, lineHeight = 35.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            }
            DetailPracticalRows(
                fullWidth = listOfNotNull(
                    area?.let { DetailPracticalRow(DetailGlyph.Landmark, s.noticeWhere, it) },
                    website?.let { DetailPracticalRow(DetailGlyph.ArrowUpRight, s.website, it.linkTitle()) }
                ),
                paired = emptyList(),
                lang = lang
            )
            website?.let {
                DetailPillButton(
                    label = strings.detail.visitWebsite,
                    glyph = DetailGlyph.ArrowUpRight,
                    filled = true,
                    height = 56.dp,
                    onClick = { onAction(ServiceDetailAction.OpenWebsite) },
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
                )
            }
            DetailTextSection(service.localizedDescription(lang).trim(), strings.detail)
            val lat = service.latitude
            val lng = service.longitude
            if (lat != null && lng != null) {
                DetailWhereItIs(
                    title = strings.detail.whereItIs,
                    name = name,
                    destination = GeoPoint(lat, lng),
                    currentLocation = currentLocation,
                    areaLabel = area,
                    openInMapsLabel = strings.detail.openInMaps,
                    onOpenMaps = { onAction(ServiceDetailAction.OpenMaps) }
                )
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
