package llc.bokadev.kompass.presentation.onboarding

import androidx.lifecycle.viewModelScope
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
import llc.bokadev.kompass.core.utils.AppPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @Test
    fun aFirstRunShowsTheIntro() = screen { vm, _ ->
        assertFalse(vm.onboardingState.value.completed)
        assertFalse(vm.onboardingState.value.isLoading)
    }

    @Test
    fun aSeenIntroStaysClosedOnTheFirstFrame() = screen(stored = mapOf("home_onboarding_seen" to "true")) { vm, _ ->
        // Read synchronously: the gate must not flash the intro for a returning visitor.
        assertTrue(vm.onboardingState.value.completed)
    }

    @Test
    fun startCompletesAndRemembersItWithTheDefaultContext() = screen { vm, prefs ->
        vm.onEvent(OnboardingEvent.OnStartClick)
        assertTrue(vm.onboardingState.value.completed)
        assertEquals("true", prefs.getString("home_onboarding_seen"))
        assertEquals("planning_ahead", prefs.getString("onboarding_context"))
    }

    @Test
    fun startKeepsAnAlreadyStoredContext() = screen(stored = mapOf("onboarding_context" to "already_here")) { vm, prefs ->
        vm.onEvent(OnboardingEvent.OnStartClick)
        assertEquals("already_here", prefs.getString("onboarding_context"))
    }

    private fun screen(
        stored: Map<String, String> = emptyMap(),
        assertions: suspend TestScope.(OnboardingViewModel, AppPreferences) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val prefs = Prefs(stored)
        val vm = OnboardingViewModel(prefs)
        try {
            assertions(vm, prefs)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class Prefs(initial: Map<String, String>) : AppPreferences {
        private val values = initial.toMutableMap()
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String) = values[key]
        override fun setString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
        override fun getSelectedLanguage() = "en"
        override fun setSelectedLanguage(language: String) = Unit
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
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
