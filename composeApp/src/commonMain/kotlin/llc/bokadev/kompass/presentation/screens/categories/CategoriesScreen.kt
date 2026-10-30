package llc.bokadev.kompass.presentation.screens.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.util.rememberAppStrings
import org.koin.compose.viewmodel.koinViewModel

/** Explore orchestrator: entry points navigate from the ViewModel (no callbacks here). */
@Composable
fun CategoriesScreen(vmKey: String = "categories") {
    val vm: CategoriesViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    CategoriesScreenContent(state = state, strings = rememberAppStrings(), onAction = vm::onAction)
}
