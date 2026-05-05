package llc.bokadev.kompass.presentation.screens.myguides

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
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyGuidesScreen(
    onBack: () -> Unit = {},
    onOpenGuide: (String) -> Unit = {},
    onOpenActivity: (String) -> Unit = {},
    onOpenItineraries: () -> Unit = {}
) {
    val vm: MyGuidesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val lang = currentAppLanguage()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("my_guides")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Premium quick access",
                title = "My Guides",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KompassTheme.colors.colorSurface),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GuideSectionTitle("Unlocked audio guides")
            }
            if (state.audioGuides.isEmpty()) {
                item {
                    GuidePlaceholder(
                        title = "No audio guides unlocked yet",
                        body = "Premium audio-ready activities will appear here for quick return."
                    )
                }
            } else {
                items(state.audioGuides, key = { it.id }) { activity ->
                    GuideCard(
                        title = activity.localizedName(lang),
                        body = activity.localizedDescription(lang),
                        action = "Open guide",
                        onClick = {
                            analytics.trackGuideOpen(activity.id, activity.cityId)
                            onOpenGuide(activity.id)
                        }
                    )
                }
            }

            item {
                GuideSectionTitle("Premium activities")
            }
            if (state.premiumActivities.isEmpty()) {
                item {
                    GuidePlaceholder(
                        title = "No premium activities available",
                        body = "Activities with deeper route context and guide access will appear here."
                    )
                }
            } else {
                items(state.premiumActivities, key = { "premium-${it.id}" }) { activity ->
                    GuideCard(
                        title = activity.localizedName(lang),
                        body = activity.localizedLocation(lang).ifBlank { activity.localizedDescription(lang) },
                        action = "View activity",
                        onClick = {
                            analytics.trackActivityView(
                                activityId = activity.id,
                                cityId = activity.cityId,
                                zone = activity.category,
                                contentOrigin = "my_guides"
                            )
                            onOpenActivity(activity.id)
                        }
                    )
                }
            }

            item {
                GuideSectionTitle("Itineraries")
            }
            item {
                GuideCard(
                    title = "Custom itineraries",
                    body = "Trip-building and structured route planning will live here once the itinerary flow is ready.",
                    action = "Open placeholder",
                    onClick = onOpenItineraries
                )
            }
        }
    }
}

@Composable
private fun GuideSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = KompassTheme.colors.colorNavy
    )
}

@Composable
private fun GuideCard(
    title: String,
    body: String,
    action: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KompassTheme.colors.colorWhite, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = KompassTheme.colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = KompassTheme.colors.colorSlate
        )
        Text(
            text = action,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = KompassTheme.colors.colorAmberDark
        )
    }
}

@Composable
private fun GuidePlaceholder(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KompassTheme.colors.colorWhite, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = KompassTheme.colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = KompassTheme.colors.colorSlate
        )
    }
}
