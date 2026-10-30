package llc.bokadev.kompass.presentation.listen

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.category_list_couldnt_refresh
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.AudioCatalogEntry
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.usecase.GetAudioCatalogUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.GROUP_OTHER
import llc.bokadev.kompass.presentation.shared.areaKeyFor
import llc.bokadev.kompass.presentation.screens.home.HomeArea

class ListenViewModel(
    private val getCatalog: GetAudioCatalogUseCase,
    private val audioRepository: AudioGuideRepository,
    private val preferences: AppPreferences,
    private val userLocationProvider: UserLocationProvider,
    connectivity: ConnectivityMonitor,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<ListenState, ListenEvent>() {

    // Offline: the Downloaded view is the default the next time the library opens (spec §4.3).
    override val initialViewState = ListenState(view = if (connectivity.isOnline.value) ListenView.ALL else ListenView.DOWNLOADED)
    private val downloadJobs = mutableMapOf<String, Job>()
    private var hasLoadedInitialData = false

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            preferences.selectedLanguageFlow.drop(1).collect { load() }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val listenState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("audio_library")
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: ListenEvent) {
        when (event) {
            ListenEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            ListenEvent.OnRetryClick -> load()
            is ListenEvent.OnViewSelect -> state.update { it.copy(view = event.view) }
            is ListenEvent.OnDownloadClick ->
                if (event.item.download == DownloadState.Downloaded) emit(ListenEvent.ConfirmRemoval(event.item.track.id))
                else toggleDownload(event.item.track.id)

            is ListenEvent.OnRemoveConfirm -> remove(event.item.track.id)
            is ListenEvent.OnTrackClick -> viewModelScope.launch {
                navigator.navigateTo(routeFor(event.source, event.id))
            }

            ListenEvent.ShowDownloadFailed, is ListenEvent.ConfirmRemoval -> Unit
        }
    }

    private fun emit(value: ListenEvent) {
        viewModelScope.launch {
            event.emit(value)
        }
    }

    private var loadJob: Job? = null

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = it.tracks.isEmpty(), error = null) }
            val location = async {
                if (userLocationProvider.hasPermission()) runCatching { userLocationProvider.getCurrentLocation() }.getOrNull() else null
            }
            getCatalog()
                .onSuccess { catalog ->
                    val lang = preferences.getSelectedLanguage()
                    val tracks = catalog.entries.mapNotNull { it.toListenTrack(lang) }
                    val area = HomeArea.restore(preferences.getString(HomeArea.PREFERENCE_KEY))
                    val current = currentAreaKey(area, location.await(), tracks)
                    state.update { it.copy(isLoading = false, tracks = orderTracks(tracks, current), isIncomplete = catalog.failedSources.isNotEmpty()) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.tracks.isEmpty()) BaseError.InlineError(Res.string.category_list_couldnt_refresh) else null
                        )
                    }
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
                    val result: Resource<Unit, NetworkError, String?> = try {
                        audioRepository.download(item.track.audioPath) { progress ->
                            if (downloadJobs[trackId]?.isActive == true) setDownload(trackId, DownloadState.Downloading(progress))
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        Resource.Error(NetworkError.UNKNOWN, error.message)
                    }
                    downloadJobs.remove(trackId)
                    // "Downloaded" only once the store confirms the file is there.
                    if (result is Resource.Success && audioRepository.isAvailableOffline(item.track.audioPath)) {
                        setDownload(trackId, DownloadState.Downloaded)
                    } else {
                        setDownload(trackId, DownloadState.NotDownloaded)
                        event.emit(ListenEvent.ShowDownloadFailed)
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
        state.update { state ->
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
internal fun routeFor(source: AudioSource, id: String): Screen = when (source) {
    AudioSource.PLACE -> Screen.PlaceDetailScreen(id)
    AudioSource.ACTIVITY -> Screen.ExperienceDetailScreen(id)
    AudioSource.STORY -> Screen.LocalFindDetailScreen(id)
}
