package llc.bokadev.kompass.presentation.screens.listen

import llc.bokadev.kompass.testutil.NoOpAnalytics
import llc.bokadev.kompass.testutil.RecordingNavigator
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.ConnectivityMonitor
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.ExperienceRepository
import llc.bokadev.kompass.domain.repository.LocalFindRepository
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import llc.bokadev.kompass.domain.usecase.GetAudioCatalogUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.presentation.navigation.Route
import llc.bokadev.kompass.presentation.screens.home.HomeArea

@OptIn(ExperimentalCoroutinesApi::class)
class ListenViewModelTest {

    private val cathedral = place("cathedral", "old_town", 42.4247, 18.7712, audio = mapOf("en" to "cathedral-en.mp3", "de" to "cathedral-de.mp3"), order = 2)
    private val nicholas = place("nicholas", "old_town", 42.4250, 18.7710, audio = mapOf("en" to "nicholas-en.mp3"), order = 1)
    private val rocks = place("rocks", "perast", 42.4860, 18.6900, audio = mapOf("en" to "rocks-en.mp3"), order = 0)
    private val silent = place("silent", "old_town", 42.4250, 18.7710, audio = null, order = 0)

    @Test fun onlyPlacesWithAudioAndTheCurrentAreaFirst() = run(area = HomeArea.PERAST) { vm, _ ->
        assertEquals(listOf("rocks", "nicholas", "cathedral"), vm.state.value.tracks.filter { it.entry.source == AudioSource.PLACE }.map { it.entry.id })
    }

    @Test fun englishFallbackIsFlaggedAndOtherLanguagesAreNoted() = run(lang = "de") { vm, _ ->
        val byId = vm.state.value.tracks.filter { it.entry.source == AudioSource.PLACE }.associateBy { it.entry.id }
        assertEquals("de", byId.getValue("cathedral").track.lang)
        assertFalse(byId.getValue("cathedral").track.isEnglishFallback)
        assertTrue(byId.getValue("cathedral").hasOtherLanguages)
        assertTrue(byId.getValue("nicholas").track.isEnglishFallback)
    }

    @Test fun downloadShowsProgressThenDownloadedOnlyOnceStored() = run { vm, audio ->
        val id = vm.state.value.tracks.first { it.entry.id == "nicholas" }.track.id
        vm.onAction(ListenAction.Download(vm.state.value.tracks.first { it.track.id == id }))
        runCurrent()
        assertEquals(DownloadState.Downloading(0.5f), vm.state.value.tracks.first { it.track.id == id }.download)
        audio.gate.complete(Unit)
        runCurrent()
        val row = vm.state.value.tracks.first { it.track.id == id }
        assertEquals(DownloadState.Downloaded, row.download)
        assertTrue(row.track.isDownloaded)
        vm.onAction(ListenAction.SelectView(ListenView.DOWNLOADED))
        assertEquals(listOf("nicholas"), vm.state.value.visible.map { it.entry.id })
    }

    @Test fun cancellingADownloadLeavesNothingBehind() = run { vm, audio ->
        val id = vm.state.value.tracks.first().track.id
        vm.onAction(ListenAction.Download(vm.state.value.tracks.first { it.track.id == id }))
        runCurrent()
        vm.onAction(ListenAction.Download(vm.state.value.tracks.first { it.track.id == id }))
        runCurrent()
        audio.gate.complete(Unit)
        runCurrent()
        assertEquals(DownloadState.NotDownloaded, vm.state.value.tracks.first { it.track.id == id }.download)
        assertTrue(audio.stored.isEmpty())
    }

    @Test fun removingADownloadGoesBackToStreaming() = run(downloaded = setOf("rocks-en.mp3")) { vm, _ ->
        val rocksTrack = vm.state.value.tracks.first { it.entry.id == "rocks" && it.entry.source == AudioSource.PLACE }
        assertEquals(DownloadState.Downloaded, rocksTrack.download)
        vm.onAction(ListenAction.ConfirmRemove(rocksTrack))
        assertEquals(DownloadState.NotDownloaded, vm.state.value.tracks.first { it.entry.id == "rocks" && it.entry.source == AudioSource.PLACE }.download)
    }

    @Test fun activityAndStoryAudioAreInTheLibraryAndOpenTheirOwnDetail() = run { vm, _ ->
        val bySource = vm.state.value.tracks.associateBy { it.entry.source to it.entry.id }
        val boat = bySource.getValue(AudioSource.ACTIVITY to "boat")
        val legend = bySource.getValue(AudioSource.STORY to "legend")
        vm.onAction(ListenAction.Open(boat.entry.source, boat.entry.id))
        vm.onAction(ListenAction.Open(legend.entry.source, legend.entry.id))
        vm.onAction(ListenAction.Open(AudioSource.PLACE, "rocks"))
        assertEquals(
            listOf(Route.ExperienceDetail("boat"), Route.LocalFindDetail("legend"), Route.PlaceDetail("rocks")),
            navigator.routes
        )
    }

    @Test fun trackIdentityKeepsSourceTypeIdAndLanguage() = run { vm, _ ->
        // A story sharing an id with a place is a different track (identity, progress, downloads).
        val ids = vm.state.value.tracks.filter { it.entry.id == "rocks" }.map { it.track.id }
        assertEquals(setOf("place:rocks:en", "story:rocks:en"), ids.toSet())
    }

    @Test fun audioOnlyInAnotherLanguageIsNotClaimed() = run(lang = "en") { vm, _ ->
        // "frenchOnly" has French audio only: no English (or fallback) track exists, so it isn't listed.
        assertTrue(vm.state.value.tracks.none { it.entry.id == "frenchOnly" })
    }

    @Test fun inTheFrenchAppTheFrenchOnlyStoryIsListedInFrench() = run(lang = "fr") { vm, _ ->
        val story = vm.state.value.tracks.single { it.entry.id == "frenchOnly" }
        assertEquals("fr", story.track.lang)
        assertFalse(story.track.isEnglishFallback)
    }

    @Test fun aFailedSourceKeepsTheRestAndSaysTheLibraryMayBeIncomplete() = run(activitiesFail = true) { vm, _ ->
        assertTrue(vm.state.value.isIncomplete)
        assertTrue(vm.state.value.tracks.none { it.entry.source == AudioSource.ACTIVITY })
        assertTrue(vm.state.value.tracks.any { it.entry.source == AudioSource.PLACE })
    }

    @Test fun offlineOpensOnTheDownloadedView() = run(online = false) { vm, _ ->
        assertEquals(ListenView.DOWNLOADED, vm.state.value.view)
        assertTrue(vm.state.value.visible.isEmpty())
    }

    private fun run(
        area: HomeArea = HomeArea.OLD_TOWN,
        lang: String = "en",
        online: Boolean = true,
        downloaded: Set<String> = emptySet(),
        activitiesFail: Boolean = false,
        assertions: suspend TestScope.(ListenViewModel, FakeAudio) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val audio = FakeAudio(downloaded.toMutableSet())
        val vm = ListenViewModel(
            getCatalog = GetAudioCatalogUseCase(
                object : PlaceRepository {
                    override suspend fun getActivePlaces() = Result.success(listOf(cathedral, nicholas, rocks, silent))
                    override suspend fun getMustSeePlaces() = Result.success(emptyList<Place>())
                    override suspend fun getPlacesByCategory(category: PlaceCategory) = Result.success(emptyList<Place>())
                    override suspend fun getPlaceById(id: String): Result<Place> = error("unused")
                },
                GetActivitiesUseCase(object : ExperienceRepository {
                    override suspend fun getActivities() =
                        if (activitiesFail) Result.failure(IllegalStateException("offline"))
                        else Result.success(listOf(Experience(id = "boat", cityId = "kotor", name = mapOf("en" to "Boat"), description = emptyMap(), audioFile = mapOf("en" to "boat-en.mp3"), latitude = 42.49, longitude = 18.69, photos = emptyList())))
                    override suspend fun getActivityById(id: String): Result<Experience> = error("unused")
                }),
                GetLocalFindsUseCase(object : LocalFindRepository {
                    override suspend fun getLocalFinds() = Result.success(listOf(story("legend", mapOf("en" to "legend-en.mp3")), story("rocks", mapOf("en" to "rocks-story-en.mp3")), story("frenchOnly", mapOf("fr" to "fo-fr.mp3"))))
                    override suspend fun getLocalFindById(id: String): Result<LocalFind> = error("unused")
                })
            ),
            audioRepository = audio,
            preferences = Prefs(lang, mapOf(HomeArea.PREFERENCE_KEY to area.preferenceValue)),
            userLocationProvider = object : UserLocationProvider {
                override fun hasPermission() = false
                override suspend fun getCurrentLocation(): GeoPoint? = null
            },
            connectivity = object : ConnectivityMonitor {
                override val isOnline = MutableStateFlow(online)
            },
            navigator = navigator,
            analytics = NoOpAnalytics()
        )
        try {
            runCurrent()
            assertions(vm, audio)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private val navigator = RecordingNavigator()

    private fun story(id: String, audio: Map<String, String>) = LocalFind(
        id = id, cityId = "kotor", title = mapOf("en" to id), body = emptyMap(), audioFile = audio, location = null,
        latitude = 42.4247, longitude = 18.7712, photos = emptyList(), isActive = true, sortOrder = 0
    )

    class FakeAudio(val stored: MutableSet<String>) : AudioGuideRepository {
        val gate = CompletableDeferred<Unit>()
        override suspend fun getSignedAudioUrl(path: String) = Result.success("url:$path")
        override suspend fun streamUrl(path: String) = Result.success("url:$path")
        override suspend fun download(path: String, onProgress: (Float?) -> Unit): Result<Unit> {
            onProgress(0.5f)
            gate.await()
            stored += path
            return Result.success(Unit)
        }
        override fun removeDownload(path: String): Boolean { stored -= path; return true }
        override fun isAvailableOffline(path: String) = path in stored
    }

    private fun place(id: String, zone: String, lat: Double, lng: Double, audio: Map<String, String>?, order: Int) = Place(
        id = id, cityId = "kotor", name = mapOf("en" to id), description = emptyMap(), longDescription = emptyMap(),
        localsTip = emptyMap(), category = PlaceCategory.SEE_AND_VISIT, subCategory = null, priceIndicator = null,
        latitude = lat, longitude = lng, zone = zone, tags = emptyList(), bestTime = BestTime.ANYTIME,
        estimatedDuration = null, openingHours = null, menuUrl = null, bookingPhone = null, photos = emptyList(),
        audioFile = audio, audioAccessTier = "free", detailAccessTier = "free", isMustSee = false, isActive = true, sortOrder = order
    )

    private class Prefs(private val lang: String, initial: Map<String, String>) : AppPreferences {
        private val values = initial.toMutableMap()
        override val selectedLanguageFlow = MutableStateFlow(lang)
        override fun getString(key: String) = values[key]
        override fun setString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
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
