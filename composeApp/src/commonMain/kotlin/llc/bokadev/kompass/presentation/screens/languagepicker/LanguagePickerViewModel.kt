package llc.bokadev.kompass.presentation.screens.languagepicker

import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.core.util.AppPreferences

data class LanguagePickerState(
    override val isLoading: Boolean = false,
    override val error: String? = null
) : BaseState()

sealed interface LanguagePickerEvent : BaseEvent {
    data class SelectLanguage(val code: String) : LanguagePickerEvent
}

sealed interface LanguagePickerSideEffect {
    data object NavigateToMain : LanguagePickerSideEffect
}

class LanguagePickerViewModel(
    private val prefs: AppPreferences
) : BaseViewModel<LanguagePickerState, LanguagePickerEvent>() {

    override val initialState = LanguagePickerState()

    override fun onIntent(event: LanguagePickerEvent) {
        when (event) {
            is LanguagePickerEvent.SelectLanguage -> {
                prefs.setSelectedLanguage(event.code)
                prefs.setFirstLaunch(false)
            }
        }
    }
}
