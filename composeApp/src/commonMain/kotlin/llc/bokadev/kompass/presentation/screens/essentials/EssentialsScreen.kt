package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EssentialsScreen(
    vmKey: String = "essentials",
    onBack: () -> Unit = {}
) {
    val vm: EssentialsViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()
    val strings = rememberAppStrings()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("essentials")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = strings.essentialsSlug,
                title = strings.essentialsTitle,
                showBack = true,
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
