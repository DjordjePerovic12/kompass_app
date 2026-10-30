package llc.bokadev.kompass.presentation.search

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.ui.theme.pageGradientBackground

/**
 * Orchestrator: ViewModel, state, autofocus on first open, and the keyboard. Results navigate from the ViewModel.
 * No BaseContentView: Search never loads or fails as a whole (the index status is shown inline under the
 * field), and BaseContentView's imePadding would lift the results above the keyboard, which they never were.
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.searchState.collectAsStateWithLifecycle()
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

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is SearchEvent.HideKeyboard) keyboard?.hide()
    }

    SearchScreenContent(
        modifier = modifier.pageGradientBackground(),
        state = state,
        listState = listState,
        focusRequester = focusRequester,
        onBackClick = { viewModel.onEvent(SearchEvent.NavigateBack) },
        onQueryChange = { viewModel.onEvent(SearchEvent.OnQueryChange(it)) },
        onQuerySubmit = { viewModel.onEvent(SearchEvent.OnQuerySubmit(it)) },
        onKindChange = { viewModel.onEvent(SearchEvent.OnKindChange(it)) },
        onResultClick = { viewModel.onEvent(SearchEvent.OnResultClick(it)) },
        onShowMoreClick = { viewModel.onEvent(SearchEvent.OnShowMoreClick) },
        onClearRecentClick = { viewModel.onEvent(SearchEvent.OnClearRecentClick) },
        onRetryClick = { viewModel.onEvent(SearchEvent.OnRetryClick) },
        onBrowseExploreClick = { viewModel.onEvent(SearchEvent.OnBrowseExploreClick) }
    )
}
