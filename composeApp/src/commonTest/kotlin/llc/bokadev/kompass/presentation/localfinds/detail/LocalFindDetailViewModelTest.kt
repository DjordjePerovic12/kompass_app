package llc.bokadev.kompass.presentation.localfinds.detail

import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import app.cash.turbine.test
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.stories_story_load_error
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
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.LocalFindRepository
import llc.bokadev.kompass.domain.usecase.GetLocalFindByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.testutil.FakeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocalFindDetailViewModelTest {

    @Test
    fun loadsTheStoryItsAudioAndTheNextStoryOnFirstCollection() = screen { vm, _, _, _ ->
        vm.localFindDetailState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("alkima", state.find?.id)
            assertNotNull(state.audioTrack)
            assertEquals(NextStory("rocks", "Rocks"), state.next)
        }
    }

    @Test
    fun failureWithNothingShownIsAnInlineErrorAndRetryRecovers() = screen(fail = true) { vm, repo, _, _ ->
        vm.localFindDetailState.test {
            advanceUntilIdle()
            assertEquals(BaseError.InlineError(Res.string.stories_story_load_error), expectMostRecentItem().error)
            repo.fail = false
            vm.onEvent(LocalFindDetailEvent.OnRetryClick)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.error)
            assertEquals("alkima", state.find?.id)
        }
    }

    @Test
    fun nextStoryReplacesThisOneAndBackNavigatesUp() = screen { vm, _, navigator, _ ->
        vm.onEvent(LocalFindDetailEvent.OnNextStoryClick("rocks"))
        vm.onEvent(LocalFindDetailEvent.NavigateBack)
        advanceUntilIdle()
        val next = navigator.recorded.first() as NavType.NavigateToRoute
        assertEquals(Screen.LocalFindDetailScreen("rocks", "LEGEND"), next.destination)
        val options = navOptions(next.navOptions)
        assertEquals(Screen.LocalFindDetailScreen::class, options.popUpToRouteClass)
        assertTrue(options.isPopUpToInclusive())
        assertEquals(NavType.NavigateUp, navigator.recorded.last())
    }

    @Test
    fun mapHandsOffThroughTheEventFlowAndScrollingNotifiesTheAudioHost() = screen { vm, _, _, navAudio ->
        vm.eventFlow.test {
            vm.onEvent(LocalFindDetailEvent.OnMapClick(42.42, 18.77, "Karampana"))
            assertEquals(LocalFindDetailEvent.OpenExternal(ExternalLaunch.Maps(42.42, 18.77, "Karampana")), awaitItem())
        }
        val before = navAudio.interactionSignal.value
        vm.onEvent(LocalFindDetailEvent.OnUserScrolled)
        assertEquals(before + 1, navAudio.interactionSignal.value)
    }

    private fun screen(
        fail: Boolean = false,
        block: suspend TestScope.(LocalFindDetailViewModel, FakeStories, FakeNavigator, NavAudioHost) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeStories(fail)
        val navigator = FakeNavigator()
        val navAudio = NavAudioHost()
        val vm = LocalFindDetailViewModel(
            id = "alkima",
            filter = "LEGEND",
            getLocalFindById = GetLocalFindByIdUseCase(repo),
            getLocalFinds = GetLocalFindsUseCase(repo),
            audioGuideRepository = object : AudioGuideRepository {
                override suspend fun streamUrl(path: String) = Resource.Success(path)
                override suspend fun download(path: String, onProgress: (Float?) -> Unit) = Resource.Success(Unit)
                override fun removeDownload(path: String) = true
                override fun isAvailableOffline(path: String) = false
            },
            preferences = Prefs(),
            navigator = navigator,
            navAudio = navAudio
        )
        try {
            block(vm, repo, navigator, navAudio)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class FakeStories(var fail: Boolean) : LocalFindRepository {
        private val items = listOf(story("alkima", 0, audio = true), story("rocks", 1), story("polako", 2))

        override suspend fun getLocalFinds(): Resource<List<LocalFind>, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.NO_INTERNET, "offline") else Resource.Success(items)

        override suspend fun getLocalFindById(id: String): Resource<LocalFind, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.NO_INTERNET, "offline")
            else items.firstOrNull { it.id == id }?.let { Resource.Success(it) } ?: Resource.Error(NetworkError.NOT_FOUND, id)

        private fun story(id: String, order: Int, audio: Boolean = false) = LocalFind(
            id = id, cityId = "kotor", title = mapOf("en" to id.replaceFirstChar { it.uppercase() }), body = mapOf("en" to "A story."),
            audioFile = if (audio) mapOf("en" to "$id.mp3") else null, location = mapOf("en" to "Old Town Kotor"),
            latitude = 42.42, longitude = 18.77, photos = emptyList(), isActive = true, sortOrder = order
        )
    }

    private class Prefs : AppPreferences {
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String): String? = null
        override fun setString(key: String, value: String?) = Unit
        override fun getSelectedLanguage() = "en"
        override fun setSelectedLanguage(language: String) = Unit
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
    }
}
