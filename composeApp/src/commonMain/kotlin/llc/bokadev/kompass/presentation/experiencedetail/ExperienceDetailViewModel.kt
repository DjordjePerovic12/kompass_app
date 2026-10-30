package llc.bokadev.kompass.presentation.experiencedetail

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_activity_load_error
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetActivityByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.screens.favorites.areaKeyFor
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class ExperienceDetailViewModel(
    /** The activity id; the screen also keys its audio ownership on it. */
    val id: String,
    private val getActivityById: GetActivityByIdUseCase,
    private val audioGuideRepository: AudioGuideRepository,
    private val appPreferences: AppPreferences,
    private val favorites: FavoritesRepository,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    private val navAudio: NavAudioHost
) : BaseViewModel<ExperienceDetailState, ExperienceDetailEvent>() {

    override val initialViewState = ExperienceDetailState()
    private var hasLoadedInitialData = false

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            appPreferences.selectedLanguageFlow.drop(1).collect {
                state.value.activity?.let { a -> state.update { it.copy(audioTrack = a.toAudioTrack()) } }
            }
        }
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                state.update { it.copy(isSaved = list.any { e -> e.type == FavoriteItemType.ACTIVITY && e.id == id }) }
            }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val experienceDetailState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadActivity()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: ExperienceDetailEvent) {
        when (event) {
            ExperienceDetailEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            ExperienceDetailEvent.OnRetryClick -> loadActivity()
            is ExperienceDetailEvent.OnSavedChange -> setSaved(event.saved, state.value.activity)
            ExperienceDetailEvent.OnMapsClick -> {
                val activity = state.value.activity ?: return
                val lat = activity.latitude ?: return
                val lng = activity.longitude ?: return
                openExternal(ExternalLaunch.Maps(lat, lng, activity.localizedName(appPreferences.getSelectedLanguage())))
            }

            is ExperienceDetailEvent.OnWebsiteClick -> openExternal(ExternalLaunch.Url(event.url))
            ExperienceDetailEvent.OnUserScrolled -> navAudio.notifyUserScrolled()
            is ExperienceDetailEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(ExperienceDetailEvent.OpenExternal(launch))
        }
    }

    /** Idempotent; on save the title/photo/area are remembered first, for Saved offline. */
    private fun setSaved(saved: Boolean, activity: Experience?) {
        if (!saved) {
            favorites.remove(FavoriteItemType.ACTIVITY, id)
            return
        }
        activity?.let { a ->
            snapshots.putAll(mapOf(FavoriteKey(FavoriteItemType.ACTIVITY, id) to a.toSavedSnapshot(areaKeyFor(a.localizedLocation("en")))))
        }
        favorites.save(FavoriteItemType.ACTIVITY, id)
    }

    private fun loadActivity() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = it.activity == null, error = null) }
            getActivityById(id)
                .onSuccess { activity ->
                    state.update { it.copy(isLoading = false, activity = activity, audioTrack = activity.toAudioTrack()) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.activity == null) BaseError.InlineError(Res.string.detail_activity_load_error) else null
                        )
                    }
                }
        }
    }

    /** Same rule as places: the app-language track, else English (flagged for the control). */
    private fun Experience.toAudioTrack(): AudioTrack? {
        val lang = appPreferences.getSelectedLanguage()
        return AudioTrack.of(AudioSource.ACTIVITY, id, localizedName(lang), localizedLocation(lang), audioFile, lang, audioGuideRepository::isAvailableOffline)
    }
}
