package llc.bokadev.kompass.presentation.languagepicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.presentation.shared.LanguageChooser
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground

/** The shared LanguageChooser, full screen and anchored to the bottom, with no close action. */
@Composable
fun LanguagePickerScreenContent(
    modifier: Modifier = Modifier,
    onLanguageSelect: (String) -> Unit,
    onUsePhoneLanguageClick: () -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    Box(modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars), contentAlignment = Alignment.BottomCenter) {
        LanguageChooser(
            currentLanguage = lang,
            // The shared chooser still takes the AppStrings tables until it is migrated.
            strings = appStringsFor(lang).language,
            onSelect = onLanguageSelect,
            onUsePhoneLanguage = onUsePhoneLanguageClick,
            onDismiss = null
        )
    }
}

@Preview
@Composable
private fun LanguagePickerScreenContentPreview() {
    KompassTheme {
        LanguagePickerScreenContent(
            modifier = Modifier.pageGradientBackground(),
            onLanguageSelect = {},
            onUsePhoneLanguageClick = {}
        )
    }
}
