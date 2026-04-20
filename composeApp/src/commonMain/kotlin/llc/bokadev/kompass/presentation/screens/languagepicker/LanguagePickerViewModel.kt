package llc.bokadev.kompass.presentation.screens.languagepicker

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import llc.bokadev.kompass.core.util.AppPreferences

data class LanguagePickerState(
    val isLoading: Boolean = false
)

sealed interface LanguagePickerIntent {
    data class SelectLanguage(val code: String) : LanguagePickerIntent
}

sealed interface LanguagePickerSideEffect {
    data object NavigateToMain : LanguagePickerSideEffect
}

class LanguagePickerViewModel(
    private val prefs: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(LanguagePickerState())
    val state: StateFlow<LanguagePickerState> = _state.asStateFlow()

    fun onIntent(intent: LanguagePickerIntent) {
        when (intent) {
            is LanguagePickerIntent.SelectLanguage -> {
                prefs.setSelectedLanguage(intent.code)
                prefs.setFirstLaunch(false)
            }
        }
    }
}
