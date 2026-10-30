package llc.bokadev.kompass.core.presentation.mvi

import androidx.compose.runtime.Composable
import llc.bokadev.kompass.core.util.AppStrings
import llc.bokadev.kompass.core.util.rememberAppStrings

/**
 * Visitor-facing text a ViewModel can hand to the UI without knowing the current language:
 * resolved against [AppStrings] at render time, so it follows language changes.
 */
fun interface UiText {
    fun resolve(strings: AppStrings): String
}

@Composable
fun UiText.asString(): String = resolve(rememberAppStrings())
