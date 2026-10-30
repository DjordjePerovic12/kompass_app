package llc.bokadev.kompass.presentation.services.detail

import androidx.compose.foundation.background
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
import kompass.composeapp.generated.resources.detail_open_in_maps
import kompass.composeapp.generated.resources.detail_visit_website
import kompass.composeapp.generated.resources.detail_where_it_is
import kompass.composeapp.generated.resources.services_title
import kompass.composeapp.generated.resources.states_notice_where
import kompass.composeapp.generated.resources.states_website
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.shared.linkTitle
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.detail.DetailPillButton
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRow
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRows
import llc.bokadev.kompass.presentation.shared.detail.DetailTextSection
import llc.bokadev.kompass.presentation.shared.detail.DetailWhereItIs
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource

/**
 * A service: area and contact first, then what it offers; the website/map only when valid data exists.
 */
@Composable
fun ServiceDetailScreenContent(
    modifier: Modifier = Modifier,
    state: ServiceDetailState,
    currentLocation: GeoPoint?,
    onBackClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onMapsClick: () -> Unit
) {
    val service = state.service ?: return
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val colors = KompassTheme.colors
    val name = service.localizedName(lang)
    val area = service.localizedLocation(lang)?.trim()?.takeIf { it.isNotBlank() }
    val website = service.externalWebsite?.trim()?.takeIf { it.isNotBlank() }
    val whereLabel = stringResource(Res.string.states_notice_where)
    val websiteLabel = stringResource(Res.string.states_website)

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LamplightCompactHeader(title = stringResource(Res.string.services_title), subtitle = null, backLabel = stringResource(Res.string.detail_back), onBack = onBackClick)
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = name,
                style = KompassTheme.typography.gloockRegular34.copy(fontSize = 32.sp, lineHeight = 35.sp),
                color = colors.boneWhite,
                modifier = Modifier.semantics { heading() }
            )
        }
        DetailPracticalRows(
            fullWidth = listOfNotNull(
                area?.let { DetailPracticalRow(DetailGlyph.Landmark, whereLabel, it) },
                website?.let { DetailPracticalRow(DetailGlyph.ArrowUpRight, websiteLabel, it.linkTitle()) }
            ),
            paired = emptyList(),
            lang = lang
        )
        website?.let {
            DetailPillButton(
                label = stringResource(Res.string.detail_visit_website),
                glyph = DetailGlyph.ArrowUpRight,
                filled = true,
                height = 56.dp,
                onClick = onWebsiteClick,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )
        }
        DetailTextSection(service.localizedDescription(lang).trim())
        val lat = service.latitude
        val lng = service.longitude
        if (lat != null && lng != null) {
            DetailWhereItIs(
                title = stringResource(Res.string.detail_where_it_is),
                name = name,
                destination = GeoPoint(lat, lng),
                currentLocation = currentLocation,
                areaLabel = area,
                openInMapsLabel = stringResource(Res.string.detail_open_in_maps),
                onOpenMaps = onMapsClick
            )
        }
        Spacer(Modifier.height(26.dp))
    }
}

@Preview
@Composable
private fun ServiceDetailScreenContentPreview() {
    KompassTheme {
        ServiceDetailScreenContent(
            modifier = Modifier.background(KompassTheme.colors.nightBlack),
            state = ServiceDetailState(
                isLoading = false,
                service = Service(
                    id = "preview",
                    name = mapOf("en" to "Bay Laundry"),
                    description = mapOf("en" to "Same-day wash and fold, pick-up in the Old Town."),
                    location = mapOf("en" to "Dobrota"),
                    externalWebsite = "https://baylaundry.me",
                    latitude = 42.44,
                    longitude = 18.76
                )
            ),
            currentLocation = null,
            onBackClick = {},
            onWebsiteClick = {},
            onMapsClick = {}
        )
    }
}
