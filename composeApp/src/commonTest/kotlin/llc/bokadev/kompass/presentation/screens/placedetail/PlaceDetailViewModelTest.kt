package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.mvi.NavigationCommand
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.testutil.FakeFavorites
import llc.bokadev.kompass.testutil.RecordingNavigator
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.utils.*
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceDetailViewModelTest {

    @Test fun trackUsesTheAppLanguageWhenItExists() = runVm(lang = "de", audio = mapOf("en" to "en.mp3", "de" to "de.mp3")) { vm ->
        val track = assertNotNull(vm.state.value.audioTrack)
        assertEquals("de.mp3", track.audioPath)
        assertEquals("de", track.lang)
        assertFalse(track.isEnglishFallback)
    }

    @Test fun missingTranslationFallsBackToEnglishAndSaysSo() = runVm(lang = "fr", audio = mapOf("en" to "en.mp3", "fr" to "")) { vm ->
        val track = assertNotNull(vm.state.value.audioTrack)
        assertEquals("en.mp3", track.audioPath)
        assertEquals("en", track.lang)
        assertTrue(track.isEnglishFallback)
    }

    @Test fun noAudioAtAllMeansNoTrack() = runVm(lang = "en", audio = null) { vm ->
        assertNotNull(vm.state.value.place)
        assertNull(vm.state.value.audioTrack)
    }

    @Test fun downloadedFlagComesFromTheOfflineStore() = runVm(lang = "en", audio = mapOf("en" to "en.mp3"), offline = setOf("en.mp3")) { vm ->
        assertTrue(assertNotNull(vm.state.value.audioTrack).isDownloaded)
    }

    @Test fun retryReloadsAfterAFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val places = FakePlaceRepository(audio = null, failFirst = true)
            val vm = model(GetPlaceByIdUseCase(places), FakeAudioRepository(emptySet()), FakeAppPreferences("en"))
            advanceUntilIdle()
            assertNotNull(vm.state.value.error)
            vm.onAction(PlaceDetailAction.Retry)
            advanceUntilIdle()
            assertNull(vm.state.value.error)
            assertNotNull(vm.state.value.place)
            vm.viewModelScope.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun localTipIsHiddenWhenItOnlyRepeatsTheDescription() {
        val place = FakePlaceRepository.place(audio = null).copy(
            description = mapOf("en" to "Climb the walls at sunrise, before the cruise ships arrive."),
            localsTip = mapOf("en" to "climb the walls at SUNRISE before the cruise ships arrive")
        )
        assertNull(place.detailLocalTip("en"))
        assertEquals("Bring water.", place.copy(localsTip = mapOf("en" to "Bring water.")).detailLocalTip("en"))
    }

    @Test fun aboutIsHiddenWhenTheLongDescriptionRepeatsTheShortOne() {
        val base = FakePlaceRepository.place(audio = null).copy(
            description = mapOf("en" to "A fish & wine restaurant with an amazing view."),
            longDescription = mapOf("en" to "A fish & wine restaurant with an amazing view.")
        )
        assertNull(base.detailAboutText("en"))
        val longer = base.copy(longDescription = mapOf("en" to "Family-run since 1987, on the water in Dobrota."))
        assertEquals("Family-run since 1987, on the water in Dobrota.", longer.detailAboutText("en"))
    }

    @Test fun savingIsExplicitAndRemembersASnapshot() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val favorites = FakeFavorites(emptyList())
            val store = mutableMapOf<FavoriteKey, SavedSnapshot>()
            val navigator = RecordingNavigator()
            val vm = model(GetPlaceByIdUseCase(FakePlaceRepository(null)), FakeAudioRepository(emptySet()), FakeAppPreferences("en"), favorites, store, navigator)
            advanceUntilIdle()
            vm.onAction(PlaceDetailAction.SetSaved(true))
            vm.onAction(PlaceDetailAction.SetSaved(true))
            advanceUntilIdle()
            assertTrue(vm.state.value.isSaved)
            assertEquals(1, favorites.favoritesFlow.value.size)
            assertNotNull(store[FavoriteKey(FavoriteItemType.PLACE, "p")])
            vm.onAction(PlaceDetailAction.Back)
            assertEquals(NavigationCommand.NavigateUp, navigator.recorded.single())
            vm.viewModelScope.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun model(
        getPlaceById: GetPlaceByIdUseCase,
        audio: AudioGuideRepository,
        prefs: AppPreferences,
        favorites: FavoritesRepository = FakeFavorites(emptyList()),
        store: MutableMap<FavoriteKey, SavedSnapshot> = mutableMapOf(),
        navigator: RecordingNavigator = RecordingNavigator()
    ) = PlaceDetailViewModel(
        id = "p",
        getPlaceById = getPlaceById,
        audioGuideRepository = audio,
        appPreferences = prefs,
        favorites = favorites,
        snapshots = object : SavedSnapshotRepository {
            override fun get(key: FavoriteKey) = store[key]
            override fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>) { store.putAll(snapshots) }
        },
        navigator = navigator,
        navAudio = NavAudioHost()
    )

    private fun runVm(
        lang: String,
        audio: Map<String, String>?,
        offline: Set<String> = emptySet(),
        assertions: (PlaceDetailViewModel) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = model(GetPlaceByIdUseCase(FakePlaceRepository(audio)), FakeAudioRepository(offline), FakeAppPreferences(lang))
            advanceUntilIdle()
            assertions(vm)
            vm.viewModelScope.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakePlaceRepository(private val audio: Map<String, String>?, private var failFirst: Boolean = false) : PlaceRepository {
        override suspend fun getActivePlaces(): Resource<List<Place>, NetworkError, String?> = Resource.Success(emptyList())
        override suspend fun getMustSeePlaces(): Resource<List<Place>, NetworkError, String?> = Resource.Success(emptyList())
        override suspend fun getPlacesByCategory(category: PlaceCategory): Resource<List<Place>, NetworkError, String?> = Resource.Success(emptyList())
        override suspend fun getPlaceById(id: String): Resource<Place, NetworkError, String?> {
            if (failFirst) {
                failFirst = false
                return Resource.Error(NetworkError.UNKNOWN, "network")
            }
            return Resource.Success(place(audio))
        }

        companion object {
            fun place(audio: Map<String, String>?) = Place(
                id = "p", cityId = "kotor", name = mapOf("en" to "Cathedral"),
                description = emptyMap(), longDescription = emptyMap(), localsTip = emptyMap(),
                category = PlaceCategory.SEE_AND_VISIT, subCategory = "cathedral", priceIndicator = null,
                latitude = 42.42, longitude = 18.77, zone = "old_town", tags = emptyList(),
                bestTime = BestTime.ANYTIME, estimatedDuration = 30, openingHours = null,
                menuUrl = null, bookingPhone = null, photos = emptyList(),
                audioFile = audio, audioAccessTier = "free", detailAccessTier = "free",
                isMustSee = false, isActive = true, sortOrder = 0
            )
        }
    }

    private class FakeAudioRepository(private val offline: Set<String>) : AudioGuideRepository {
        override suspend fun getSignedAudioUrl(path: String): Resource<String, NetworkError, String?> = Resource.Success("https://signed/$path")
        override fun isAvailableOffline(path: String): Boolean = path in offline
        override suspend fun streamUrl(path: String): Resource<String, NetworkError, String?> = getSignedAudioUrl(path)
        override suspend fun download(path: String, onProgress: (Float?) -> Unit): Resource<Unit, NetworkError, String?> = Resource.Success(Unit)
        override fun removeDownload(path: String): Boolean = true
    }

    private class FakeAppPreferences(private val lang: String) : AppPreferences {
        private val values = mutableMapOf<String, String>()
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
