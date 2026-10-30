package llc.bokadev.kompass.presentation.languagepicker

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.supportedDeviceLanguage
import llc.bokadev.kompass.presentation.base.BaseViewModel

/**
 * First launch: stores the language, marks first launch done and enters the app. This screen sits
 * on the root NavHost; its destination bridges the injected [Navigator] to the root controller.
 */
class LanguagePickerViewModel(
    private val prefs: AppPreferences,
    private val navigator: Navigator
) : BaseViewModel<LanguagePickerState, LanguagePickerEvent>() {

    override val initialViewState = LanguagePickerState()

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val languagePickerState: StateFlow<LanguagePickerState> = state

    fun onEvent(event: LanguagePickerEvent) {
        val code = when (event) {
            is LanguagePickerEvent.OnLanguageSelect -> event.code
            LanguagePickerEvent.OnUsePhoneLanguageClick -> supportedDeviceLanguage()
        }
        prefs.setSelectedLanguage(code)
        prefs.setFirstLaunch(false)
        viewModelScope.launch {
            navigator.navigateTo(Screen.MainScreen) {
                popUpTo<Screen.LanguagePickerScreen> { inclusive = true }
            }
        }
    }
}
