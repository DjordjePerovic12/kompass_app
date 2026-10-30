package llc.bokadev.kompass.presentation.screens.listen

import kotlinx.coroutines.flow.drop
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.navigation.Route
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.ConnectivityMonitor
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.AudioCatalogEntry
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.usecase.GetAudioCatalogUseCase
import llc.bokadev.kompass.presentation.screens.favorites.GROUP_OTHER
import llc.bokadev.kompass.presentation.screens.favorites.areaKeyFor
import llc.bokadev.kompass.presentation.screens.home.HomeArea

enum class ListenView { ALL, DOWNLOADED }

sealed interface DownloadState {
    data object NotDownloaded : DownloadState
    /** [progress] is 0..1, or null while the size isn't known yet. */
    data class Downloading(val progress: Float?) : DownloadState
    data object Downloaded : DownloadState
}

/** One library row: a place's, activity's or story's free audio in the app language (English fallback). */
data class ListenTrack(
    val track: AudioTrack,
    val entry: AudioCatalogEntry,
    val areaKey: String,
    /** The item has audio in other languages too (the remove dialog says only this one goes). */
    val hasOtherLanguages: Boolean,
    val download: DownloadState
)

class ListenViewModel(
    private val getCatalog: GetAudioCatalogUseCase,
    private val audioRepository: AudioGuideRepository,
    private val preferences: AppPreferences,
    private val userLocationProvider: UserLocationProvider,
    connectivity: ConnectivityMonitor,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<ListenState, ListenAction, ListenEvent>(
    // Offline: the Downloaded view is the default the next time the library opens (spec §4.3).
    ListenState(view = if (connectivity.isOnline.value) ListenView.ALL else ListenView.DOWNLOADED)
) {

    private val downloadJobs = mutableMapOf<String, Job>()

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            preferences.selectedLanguageFlow.drop(1).collect { load() }
        }
    }

    override suspend fun onFirstStart() {
        analytics.trackScreenView("audio_library")
        load()
    }

    override fun onAction(action: ListenAction) {
        when (action) {
            is ListenAction.SelectView -> updateState { it.copy(view = action.view) }
            is ListenAction.Download ->
                if (action.item.download == DownloadState.Downloaded) sendEvent(ListenEvent.ConfirmRemoval(action.item.track.id))
                else toggleDownload(action.item.track.id)
            is ListenAction.ConfirmRemove -> remove(action.item.track.id)
            is ListenAction.Open -> navigator.navigateTo(routeFor(action.source, action.id))
            ListenAction.Retry -> load()
            ListenAction.Back -> navigator.navigateUp()
            // Playback and the remove dialog belong to the screen's orchestrator.
            ListenAction.DismissRemove, ListenAction.OpenCurrent, is ListenAction.Play, ListenAction.ToggleCurrent,
            ListenAction.Stop, is ListenAction.Seek, is ListenAction.Skip -> Unit
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = it.tracks.isEmpty(), error = null) }
            val location = async {
                if (userLocationProvider.hasPermission()) runCatching { userLocationProvider.getCurrentLocation() }.getOrNull() else null
            }
            getCatalog()
                .onSuccess { catalog ->
                    val lang = preferences.getSelectedLanguage()
                    val tracks = catalog.entries.mapNotNull { it.toListenTrack(lang) }
                    val area = HomeArea.restore(preferences.getString(HomeArea.PREFERENCE_KEY))
                    val current = currentAreaKey(area, location.await(), tracks)
                    updateState { it.copy(isLoading = false, tracks = orderTracks(tracks, current), isIncomplete = catalog.failedSources.isNotEmpty()) }
                }
                .onFailure {
                    updateState { it.copy(isLoading = false, error = if (it.tracks.isEmpty()) UiText { s -> s.categoryList.couldntRefresh } else null) }
                }
        }
    }

    private fun AudioCatalogEntry.toListenTrack(lang: String): ListenTrack? {
        val subtitle = zone?.takeIf { it.isNotBlank() } ?: localizedArea(lang)
        val track = AudioTrack.of(source, id, localizedTitle(lang), subtitle, files, lang, audioRepository::isAvailableOffline) ?: return null
        return ListenTrack(
            track = track,
            entry = this,
            areaKey = areaKeyFor(zone),
            hasOtherLanguages = languages.size > 1,
            download = if (track.isDownloaded) DownloadState.Downloaded else DownloadState.NotDownloaded
        )
    }

    private fun toggleDownload(trackId: String) {
        val item = state.value.tracks.firstOrNull { it.track.id == trackId } ?: return
        when (item.download) {
            DownloadState.Downloaded -> Unit
            is DownloadState.Downloading -> {
                downloadJobs.remove(trackId)?.cancel()
                setDownload(trackId, DownloadState.NotDownloaded)
            }
            DownloadState.NotDownloaded -> {
                setDownload(trackId, DownloadState.Downloading(null))
                downloadJobs[trackId] = viewModelScope.launch {
                    val result = try {
                        audioRepository.download(item.track.audioPath) { progress ->
                            if (downloadJobs[trackId]?.isActive == true) setDownload(trackId, DownloadState.Downloading(progress))
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        Result.failure(error)
                    }
                    downloadJobs.remove(trackId)
                    // "Downloaded" only once the store confirms the file is there.
                    if (result.isSuccess && audioRepository.isAvailableOffline(item.track.audioPath)) {
                        setDownload(trackId, DownloadState.Downloaded)
                    } else {
                        setDownload(trackId, DownloadState.NotDownloaded)
                        sendEvent(ListenEvent.DownloadFailed)
                    }
                }
            }
        }
    }

    private fun remove(trackId: String) {
        val item = state.value.tracks.firstOrNull { it.track.id == trackId } ?: return
        audioRepository.removeDownload(item.track.audioPath)
        setDownload(trackId, if (audioRepository.isAvailableOffline(item.track.audioPath)) DownloadState.Downloaded else DownloadState.NotDownloaded)
    }

    private fun setDownload(trackId: String, download: DownloadState) {
        updateState { state ->
            state.copy(tracks = state.tracks.map {
                if (it.track.id != trackId) it
                else it.copy(download = download, track = it.track.copy(isDownloaded = download == DownloadState.Downloaded))
            })
        }
    }
}

/** Home's chosen area, else the area of the track nearest the device, else none. */
internal fun currentAreaKey(area: HomeArea, location: GeoPoint?, tracks: List<ListenTrack>): String? {
    if (area != HomeArea.DEVICE) return area.preferenceValue
    location ?: return null
    return tracks
        .filter { it.entry.latitude != null && it.entry.longitude != null }
        .minByOrNull { location.distanceKmTo(GeoPoint(it.entry.latitude!!, it.entry.longitude!!)) }?.areaKey
}

/**
 * Current area first, then Old Town, Dobrota, Perast, other areas A–Z, Other;
 * editorial order inside an area.
 */
internal fun orderTracks(tracks: List<ListenTrack>, currentArea: String?): List<ListenTrack> {
    val curated = listOf(HomeArea.OLD_TOWN, HomeArea.DOBROTA, HomeArea.PERAST).map { it.preferenceValue }
    return tracks.sortedWith(
        compareBy<ListenTrack> { if (it.areaKey == currentArea) 0 else 1 }
            .thenBy {
                when (it.areaKey) {
                    in curated -> curated.indexOf(it.areaKey)
                    GROUP_OTHER -> 1_000
                    else -> 100
                }
            }
            .thenBy { it.areaKey }
            .thenBy { it.entry.source.ordinal }
            .thenBy { it.entry.sortOrder }
    )
}

/** Where a library row (or the Now playing card) opens: the item's own detail. */
internal fun routeFor(source: AudioSource, id: String): Route = when (source) {
    AudioSource.PLACE -> Route.PlaceDetail(id)
    AudioSource.ACTIVITY -> Route.ExperienceDetail(id)
    AudioSource.STORY -> Route.LocalFindDetail(id)
}
