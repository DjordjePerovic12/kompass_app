package llc.bokadev.kompass.presentation.screens.essentials

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
fun EssentialsScreen(
    vmKey: String = "essentials",
    showBack: Boolean = true,
    onBack: () -> Unit = {}
) {
    val vm: EssentialsViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("essentials")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Kotor",
                title = "City Essentials",
                showBack = showBack,
                onBackClick = onBack
            )
        }
    ) {
        EssentialsScreenContent(
            state = state,
            onIntent = vm::onIntent
        )
    }
}
