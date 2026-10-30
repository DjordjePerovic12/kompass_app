package llc.bokadev.kompass.presentation.categorylist

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.category_list_removed_toast
import kompass.composeapp.generated.resources.category_list_saved_toast
import kompass.composeapp.generated.resources.saved_undo
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.presentation.SnackbarMessage
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

/**
 * Orchestrator: ViewModel, state, and the save/remove confirmations. Saving happens in place
 * (owner decision, 2026-10-07): the row keeps its position and the viewport doesn't move.
 */
@Composable
fun CategoryListScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoryListViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.categoryListState.collectAsStateWithLifecycle()
    // The confirmations carry a duration and an Undo action, which the String snackbar can't.
    val snackbarBus = koinInject<SnackbarBus>()
    val lang = LocalAppLocale.current.substringBefore('-')
    val listState = rememberLazyListState()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        when (event) {
            is CategoryListEvent.ShowSaved -> {
                val name = event.item.place?.localizedName(lang) ?: event.item.activity?.localizedName(lang).orEmpty()
                snackbarBus.show(SnackbarMessage(getString(Res.string.category_list_saved_toast, name), durationMs = 2_200))
            }

            is CategoryListEvent.ShowRemoved -> snackbarBus.show(
                SnackbarMessage(
                    text = getString(Res.string.category_list_removed_toast),
                    actionLabel = getString(Res.string.saved_undo),
                    onAction = { viewModel.onEvent(CategoryListEvent.OnUndoClick(event.entry)) }
                )
            )

            else -> Unit
        }
    }

    // The list keeps its header while loading or failing (the empty/error row is part of it),
    // so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        CategoryListScreenContent(
            state = state,
            listState = listState,
            onBackClick = { viewModel.onEvent(CategoryListEvent.NavigateBack) },
            onSearchClick = { viewModel.onEvent(CategoryListEvent.OnSearchClick) },
            onAreaChange = { viewModel.onEvent(CategoryListEvent.OnAreaChange(it)) },
            onItemClick = { viewModel.onEvent(CategoryListEvent.OnItemClick(it)) },
            onSavedChange = { item, saved -> viewModel.onEvent(CategoryListEvent.OnSavedChange(item, saved)) },
            onRetryClick = { viewModel.onEvent(CategoryListEvent.OnRetryClick) }
        )
    }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state,
        loading = content,
        error = { content() }
    ) {
        content()
    }
}
