package llc.bokadev.kompass.presentation.screens.experiences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExperiencesScreen(
    vmKey: String = "experiences",
    onNavigateToExperienceDetail: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val vm: ExperiencesViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("activities")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Go beyond the old town",
                title = "Activities",
                showBack = onBack != null,
                onBackClick = { onBack?.invoke() }
            )
        }
    ) {
        ExperiencesScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onActivityClick = { id ->
                state.activities.firstOrNull { it.id == id }?.let { activity ->
                    analytics.trackActivityView(
                        activityId = activity.id,
                        cityId = activity.cityId,
                        zone = activity.category,
                        contentOrigin = "activities_tab"
                    )
                }
                onNavigateToExperienceDetail(id)
            }
        )
    }
}
