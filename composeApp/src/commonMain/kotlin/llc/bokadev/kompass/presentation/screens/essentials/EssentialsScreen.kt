package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EssentialsScreen(
    onBack: () -> Unit = {}
) {
    val vm: EssentialsViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Kotor",
                title = "City Essentials",
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
