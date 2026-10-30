package llc.bokadev.kompass.presentation.screens.languagepicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.LanguageChooser
import org.koin.compose.viewmodel.koinViewModel
import llc.bokadev.kompass.ui.theme.pageGradientBackground

/** First launch: the same language controls as the in-app sheet, full screen, with nothing to close. */
@Composable
fun LanguagePickerScreen() {
    val vm: LanguagePickerViewModel = koinViewModel()
    val strings = rememberAppStrings()
    Box(Modifier.fillMaxSize().pageGradientBackground().windowInsetsPadding(WindowInsets.statusBars), contentAlignment = Alignment.BottomCenter) {
        LanguageChooser(
            currentLanguage = currentAppLanguage(),
            strings = strings.language,
            onSelect = { vm.onAction(LanguagePickerAction.SelectLanguage(it)) },
            onUsePhoneLanguage = { vm.onAction(LanguagePickerAction.UsePhoneLanguage) },
            onDismiss = null
        )
    }
}
