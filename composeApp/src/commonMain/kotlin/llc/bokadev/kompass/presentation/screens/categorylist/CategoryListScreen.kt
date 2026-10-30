package llc.bokadev.kompass.presentation.screens.categorylist

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.presentation.SnackbarMessage
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Orchestrator: ViewModel, state, and the save/remove confirmations. Saving happens in place
 * (owner decision, 2026-10-07): the row keeps its position and the viewport doesn't move.
 */
@Composable
fun CategoryListScreen(kind: String) {
    val vm: CategoryListViewModel = koinViewModel(key = "category-list-$kind") { parametersOf(kind) }
    val state by vm.state.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val listState = rememberLazyListState()

    fun nameOf(item: ListItem) = item.place?.localizedName(lang) ?: item.activity?.localizedName(lang).orEmpty()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is CategoryListEvent.Saved -> snackbar.show(
                    SnackbarMessage(strings.categoryList.savedToast.replace("{name}", nameOf(event.item)), durationMs = 2_200)
                )
                is CategoryListEvent.Removed -> snackbar.show(
                    SnackbarMessage(
                        text = strings.categoryList.removedToast,
                        actionLabel = strings.saved.undo,
                        onAction = { vm.onAction(CategoryListAction.Undo(event.entry)) }
                    )
                )
            }
        }
    }

    CategoryListScreenContent(state = state, listState = listState, onAction = vm::onAction)
}
