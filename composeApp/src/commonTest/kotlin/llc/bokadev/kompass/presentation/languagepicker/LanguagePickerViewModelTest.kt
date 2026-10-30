package llc.bokadev.kompass.presentation.languagepicker

import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.supportedDeviceLanguage
import llc.bokadev.kompass.testutil.FakeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LanguagePickerViewModelTest {

    @Test
    fun choosingALanguageStoresItEndsFirstLaunchAndEntersTheAppWithoutThePicker() = screen { vm, prefs, navigator ->
        vm.onEvent(LanguagePickerEvent.OnLanguageSelect("de"))
        advanceUntilIdle()
        assertEquals("de", prefs.getSelectedLanguage())
        assertFalse(prefs.isFirstLaunch())
        val entry = navigator.recorded.single() as NavType.NavigateToRoute
        assertEquals(Screen.MainScreen, entry.destination)
        val options = navOptions(entry.navOptions)
        assertEquals(Screen.LanguagePickerScreen::class, options.popUpToRouteClass)
        assertTrue(options.isPopUpToInclusive())
    }

    @Test
    fun usePhoneLanguageStoresTheSupportedDeviceLanguage() = screen { vm, prefs, navigator ->
        vm.onEvent(LanguagePickerEvent.OnUsePhoneLanguageClick)
        advanceUntilIdle()
        assertEquals(supportedDeviceLanguage(), prefs.getSelectedLanguage())
        assertFalse(prefs.isFirstLaunch())
        assertEquals(listOf<Screen>(Screen.MainScreen), navigator.destinations)
    }

    @Test
    fun nothingLoadsSoThePickerShowsStraightAway() = screen { vm, _, navigator ->
        val state = vm.languagePickerState.value
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
        assertTrue(navigator.recorded.isEmpty())
    }

    private fun screen(block: suspend TestScope.(LanguagePickerViewModel, Prefs, FakeNavigator) -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val prefs = Prefs()
        val navigator = FakeNavigator()
        val vm = LanguagePickerViewModel(prefs, navigator)
        try {
            block(vm, prefs, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class Prefs : AppPreferences {
        private var firstLaunch = true
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String): String? = null
        override fun setString(key: String, value: String?) = Unit
        override fun getSelectedLanguage() = selectedLanguageFlow.value
        override fun setSelectedLanguage(language: String) { selectedLanguageFlow.value = language }
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = firstLaunch
        override fun setFirstLaunch(value: Boolean) { firstLaunch = value }
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
        override fun hasAudioPass() = false
        override fun setAudioPass(value: Boolean) = Unit
        override fun hasExplorerPass() = false
        override fun setExplorerPass(value: Boolean) = Unit
        override fun hasPerksPass() = false
        override fun setPerksPass(value: Boolean) = Unit
    }
}
