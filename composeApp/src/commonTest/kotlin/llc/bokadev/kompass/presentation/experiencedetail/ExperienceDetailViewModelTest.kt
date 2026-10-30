package llc.bokadev.kompass.presentation.experiencedetail

import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.testutil.FakeFavorites
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.testutil.FakeNavigator
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.ExperienceRepository
import llc.bokadev.kompass.domain.usecase.GetActivityByIdUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ExperienceDetailViewModelTest {

    private val kayak = Experience(
        id = "kayak", cityId = "kotor", name = mapOf("en" to "Sea kayak", "de" to "Seekajak"),
        description = mapOf("en" to "Paddle the bay."), location = mapOf("en" to "Dobrota"),
        audioFile = mapOf("en" to "kayak-en.mp3"), photos = emptyList()
    )

    @Test fun loadsTheActivityWithAnEnglishFallbackTrack() = run(result = Resource.Success(kayak), lang = "de", offline = setOf("kayak-en.mp3")) { vm ->
        val state = vm.experienceDetailState.value
        assertFalse(state.isLoading)
        assertEquals("kayak", state.activity?.id)
        val track = assertNotNull(state.audioTrack)
        assertEquals("Seekajak", track.title)
        assertEquals("en", track.lang)
        assertTrue(track.isEnglishFallback)
        assertTrue(track.isDownloaded)
    }

    @Test fun noAudioMeansNoTrack() = run(result = Resource.Success(kayak.copy(audioFile = mapOf("en" to " ")))) { vm ->
        assertNull(vm.experienceDetailState.value.audioTrack)
    }

    @Test fun failureShowsAnErrorAndRetryReloads() {
        var calls = 0
        run(result = null, onCall = { calls++; if (calls == 1) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(kayak) }) { vm ->
            assertNotNull(vm.experienceDetailState.value.error)
            assertNull(vm.experienceDetailState.value.activity)
            vm.onEvent(ExperienceDetailEvent.OnRetryClick)
            testScheduler.runCurrent()
            assertNull(vm.experienceDetailState.value.error)
            assertEquals("kayak", vm.experienceDetailState.value.activity?.id)
        }
    }

    @Test fun backNavigatesUp() {
        val navigator = FakeNavigator()
        run(result = Resource.Success(kayak), navigator = navigator) { vm ->
            vm.onEvent(ExperienceDetailEvent.NavigateBack)
            runCurrent()
            assertEquals(listOf<NavType>(NavType.NavigateUp), navigator.recorded)
        }
    }

    @Test fun websiteHandsOffThroughTheEventFlow() = run(result = Resource.Success(kayak)) { vm ->
        val events = mutableListOf<ExperienceDetailEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.collect { events += it } }
        vm.onEvent(ExperienceDetailEvent.OnWebsiteClick("https://kayak.example"))
        runCurrent()
        assertEquals(listOf<ExperienceDetailEvent>(ExperienceDetailEvent.OpenExternal(ExternalLaunch.Url("https://kayak.example"))), events)
    }

    private fun run(
        result: Resource<Experience, NetworkError, String?>?,
        lang: String = "en",
        offline: Set<String> = emptySet(),
        onCall: () -> Resource<Experience, NetworkError, String?> = { result!! },
        navigator: FakeNavigator = FakeNavigator(),
        assertions: TestScope.(ExperienceDetailViewModel) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = ExperienceDetailViewModel(
            id = "kayak",
            getActivityById = GetActivityByIdUseCase(object : ExperienceRepository {
                override suspend fun getActivities() = Resource.Success(emptyList<Experience>())
                override suspend fun getActivityById(id: String) = onCall()
            }),
            audioGuideRepository = object : AudioGuideRepository {
                override suspend fun getSignedAudioUrl(path: String) = Resource.Success(path)
                override suspend fun streamUrl(path: String) = Resource.Success(path)
                override suspend fun download(path: String, onProgress: (Float?) -> Unit) = Resource.Success(Unit)
                override fun removeDownload(path: String) = true
                override fun isAvailableOffline(path: String) = path in offline
            },
            appPreferences = Prefs(lang),
            favorites = FakeFavorites(emptyList()),
            snapshots = object : SavedSnapshotRepository {
                override fun get(key: FavoriteKey): SavedSnapshot? = null
                override fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>) = Unit
            },
            navigator = navigator,
            navAudio = NavAudioHost()
        )
        try {
            // The activity loads when the screen first collects its state.
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.experienceDetailState.collect {} }
            runCurrent()
            assertions(vm)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class Prefs(private val lang: String) : AppPreferences {
        override val selectedLanguageFlow = MutableStateFlow(lang)
        override fun getString(key: String): String? = null
        override fun setString(key: String, value: String?) = Unit
        override fun getSelectedLanguage() = lang
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
