package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.presentation.screens.favorites.FavoritesViewModelTest
import llc.bokadev.kompass.testutil.RecordingNavigator
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
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("kayak", state.activity?.id)
        val track = assertNotNull(state.audioTrack)
        assertEquals("Seekajak", track.title)
        assertEquals("en", track.lang)
        assertTrue(track.isEnglishFallback)
        assertTrue(track.isDownloaded)
    }

    @Test fun noAudioMeansNoTrack() = run(result = Resource.Success(kayak.copy(audioFile = mapOf("en" to " ")))) { vm ->
        assertNull(vm.state.value.audioTrack)
    }

    @Test fun failureShowsAnErrorAndRetryReloads() {
        var calls = 0
        run(result = null, onCall = { calls++; if (calls == 1) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(kayak) }) { vm ->
            assertNotNull(vm.state.value.error)
            assertNull(vm.state.value.activity)
            vm.onAction(ExperienceDetailAction.Retry)
            testScheduler.runCurrent()
            assertNull(vm.state.value.error)
            assertEquals("kayak", vm.state.value.activity?.id)
        }
    }

    private fun run(
        result: Resource<Experience, NetworkError, String?>?,
        lang: String = "en",
        offline: Set<String> = emptySet(),
        onCall: () -> Resource<Experience, NetworkError, String?> = { result!! },
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
            favorites = FavoritesViewModelTest.FakeFavorites(emptyList()),
            snapshots = object : SavedSnapshotRepository {
                override fun get(key: FavoriteKey): SavedSnapshot? = null
                override fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>) = Unit
            },
            navigator = RecordingNavigator(),
            navAudio = NavAudioHost()
        )
        try {
            runCurrent()
            assertions(vm)
        } finally {
            vm.viewModelScope.cancel()
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
