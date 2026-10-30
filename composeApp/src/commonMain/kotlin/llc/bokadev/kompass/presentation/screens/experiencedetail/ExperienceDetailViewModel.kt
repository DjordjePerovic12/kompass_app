package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.AudioSource
import kotlinx.coroutines.flow.drop
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetActivityByIdUseCase
import llc.bokadev.kompass.presentation.screens.favorites.areaKeyFor
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class ExperienceDetailViewModel(
    private val id: String,
    private val getActivityById: GetActivityByIdUseCase,
    private val audioGuideRepository: AudioGuideRepository,
    private val appPreferences: AppPreferences,
    private val favorites: FavoritesRepository,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    private val navAudio: NavAudioHost
) : BaseViewModel<ExperienceDetailState, ExperienceDetailAction, ExperienceDetailEvent>(ExperienceDetailState()) {

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            appPreferences.selectedLanguageFlow.drop(1).collect { state.value.activity?.let { a -> updateState { it.copy(audioTrack = a.toAudioTrack()) } } }
        }
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                updateState { it.copy(isSaved = list.any { e -> e.type == FavoriteItemType.ACTIVITY && e.id == id }) }
            }
        }
    }

    override suspend fun onFirstStart() = load()

    override fun onAction(action: ExperienceDetailAction) {
        val activity = state.value.activity
        when (action) {
            ExperienceDetailAction.Retry -> viewModelScope.launch { load() }
            ExperienceDetailAction.Back -> navigator.navigateUp()
            is ExperienceDetailAction.SetSaved -> setSaved(action.saved, activity)
            ExperienceDetailAction.OpenMaps -> {
                val lat = activity?.latitude ?: return
                val lng = activity.longitude ?: return
                sendEvent(ExperienceDetailEvent.Launch(ExternalLaunch.Maps(lat, lng, activity.localizedName(appPreferences.getSelectedLanguage()))))
            }
            is ExperienceDetailAction.OpenWebsite -> sendEvent(ExperienceDetailEvent.Launch(ExternalLaunch.Url(action.url)))
            ExperienceDetailAction.UserScrolled -> navAudio.notifyUserScrolled()
            ExperienceDetailAction.Listen -> Unit // playback is owned by the orchestrator
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

    private suspend fun load() {
        updateState { it.copy(isLoading = it.activity == null, error = null) }
        getActivityById(id)
            .onSuccess { activity -> updateState { it.copy(isLoading = false, activity = activity, audioTrack = activity.toAudioTrack()) } }
            .onError { _, _ -> updateState { it.copy(isLoading = false, error = if (it.activity == null) LOAD_FAILED else null) } }
    }

    /** Same rule as places: the app-language track, else English (flagged for the control). */
    private fun Experience.toAudioTrack(): AudioTrack? {
        val lang = appPreferences.getSelectedLanguage()
        return AudioTrack.of(AudioSource.ACTIVITY, id, localizedName(lang), localizedLocation(lang), audioFile, lang, audioGuideRepository::isAvailableOffline)
    }

    private companion object {
        val LOAD_FAILED = UiText { it.detail.activityLoadError }
    }
}
