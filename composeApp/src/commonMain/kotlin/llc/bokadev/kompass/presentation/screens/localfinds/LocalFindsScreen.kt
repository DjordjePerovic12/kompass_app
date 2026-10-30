package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import org.koin.compose.viewmodel.koinViewModel

/** Legends & Localisms orchestrator; the ViewModel opens stories and navigates back. */
@Composable
fun LocalFindsScreen() {
    val vm: LocalFindsViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    StatusBarIconsEffect(lightIcons = false)
    LocalFindsScreenContent(
        state = state,
        strings = rememberAppStrings(),
        lang = currentAppLanguage(),
        onAction = vm::onAction
    )
}
