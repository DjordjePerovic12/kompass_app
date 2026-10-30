package llc.bokadev.kompass.presentation.screens.languagepicker

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.supportedDeviceLanguage
import llc.bokadev.kompass.core.navigation.Screen

data class LanguagePickerState(
    override val isLoading: Boolean = false,
    override val error: UiText? = null
) : BaseViewState

sealed interface LanguagePickerAction : BaseAction {
    data class SelectLanguage(val code: String) : LanguagePickerAction
    data object UsePhoneLanguage : LanguagePickerAction
}

/** First launch: stores the language, marks first launch done and enters the app. */
class LanguagePickerViewModel(
    private val prefs: AppPreferences,
    private val navigator: Navigator
) : BaseViewModel<LanguagePickerState, LanguagePickerAction, Nothing>(LanguagePickerState()) {

    override fun onAction(action: LanguagePickerAction) {
        val code = when (action) {
            is LanguagePickerAction.SelectLanguage -> action.code
            LanguagePickerAction.UsePhoneLanguage -> supportedDeviceLanguage()
        }
        prefs.setSelectedLanguage(code)
        prefs.setFirstLaunch(false)
        navigator.navigateTo(Screen.MainScreen, popUpToRoute = Screen.LanguagePickerScreen, popUpToInclusive = true)
    }
}
