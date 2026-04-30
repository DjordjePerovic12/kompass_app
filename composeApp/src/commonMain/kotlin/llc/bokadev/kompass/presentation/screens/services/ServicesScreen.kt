package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ServicesScreen(
    onBack: () -> Unit = {}
) {
    val vm: ServicesViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Kotor",
                title = "Services",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        ServicesScreenContent(
            state = state,
            onIntent = vm::onIntent
        )
    }
}
