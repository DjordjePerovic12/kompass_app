package llc.bokadev.kompass.presentation.screens.search

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import org.koin.compose.viewmodel.koinViewModel

/** Orchestrator: ViewModel, state, autofocus on first open, and the keyboard. Results navigate from the ViewModel. */
@Composable
fun SearchScreen() {
    val vm: SearchViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    // Autofocus only on first open; coming back from a result keeps the keyboard down.
    var autofocused by rememberSaveable { mutableStateOf(false) }

    StatusBarIconsEffect(lightIcons = false)
    LaunchedEffect(Unit) {
        if (!autofocused) {
            autofocused = true
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
    }
    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                SearchEvent.HideKeyboard -> keyboard?.hide()
            }
        }
    }

    SearchScreenContent(state = state, listState = listState, focusRequester = focusRequester, onAction = vm::onAction)
}
