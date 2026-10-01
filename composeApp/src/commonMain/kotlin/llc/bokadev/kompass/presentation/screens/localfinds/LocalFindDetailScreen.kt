package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LocalFindDetailScreen(
    id: String,
    onBack: () -> Unit
) {
    val vm: LocalFindDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()

    LocalFindDetailScreenContent(
        state = state,
        onIntent = vm::onIntent,
        onBack = onBack
    )
}
