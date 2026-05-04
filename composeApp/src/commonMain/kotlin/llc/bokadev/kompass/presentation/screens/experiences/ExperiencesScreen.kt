package llc.bokadev.kompass.presentation.screens.experiences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExperiencesScreen(
    vmKey: String = "experiences",
    onNavigateToExperienceDetail: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val vm: ExperiencesViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()

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
            onActivityClick = onNavigateToExperienceDetail
        )
    }
}
