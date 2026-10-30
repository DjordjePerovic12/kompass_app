package llc.bokadev.kompass.presentation.languagepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.ui.theme.pageGradientBackground

/** First launch: the same language controls as the in-app sheet, full screen, with nothing to close. */
@Composable
fun LanguagePickerScreen(
    modifier: Modifier = Modifier,
    viewModel: LanguagePickerViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.languagePickerState.collectAsStateWithLifecycle()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state
    ) {
        LanguagePickerScreenContent(
            onLanguageSelect = { viewModel.onEvent(LanguagePickerEvent.OnLanguageSelect(it)) },
            onUsePhoneLanguageClick = { viewModel.onEvent(LanguagePickerEvent.OnUsePhoneLanguageClick) }
        )
    }
}
