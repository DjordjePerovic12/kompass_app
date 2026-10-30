package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.domain.model.AudioSource
import kotlinx.coroutines.flow.drop
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetPlaceByIdUseCase
import llc.bokadev.kompass.presentation.screens.favorites.areaKeyFor
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class PlaceDetailViewModel(
    private val id: String,
    private val getPlaceById: GetPlaceByIdUseCase,
    private val audioGuideRepository: AudioGuideRepository,
    private val appPreferences: AppPreferences,
    private val favorites: FavoritesRepository,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    private val navAudio: NavAudioHost
) : BaseViewModel<PlaceDetailState, PlaceDetailAction, PlaceDetailEvent>(PlaceDetailState()) {

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            appPreferences.selectedLanguageFlow.drop(1).collect { state.value.place?.let { p -> updateState { it.copy(audioTrack = p.toAudioTrack()) } } }
        }
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                updateState { it.copy(isSaved = list.any { e -> e.type == FavoriteItemType.PLACE && e.id == id }) }
            }
        }
    }

    override suspend fun onFirstStart() = load()

    override fun onAction(action: PlaceDetailAction) {
        val place = state.value.place
        when (action) {
            PlaceDetailAction.Retry -> viewModelScope.launch { load() }
            PlaceDetailAction.Back -> navigator.navigateUp()
            is PlaceDetailAction.SetSaved -> setSaved(action.saved, place)
            PlaceDetailAction.OpenMaps -> place?.let {
                sendEvent(PlaceDetailEvent.Launch(ExternalLaunch.Maps(it.latitude, it.longitude, it.localizedName(appPreferences.getSelectedLanguage()))))
            }
            is PlaceDetailAction.OpenMenu -> sendEvent(PlaceDetailEvent.Launch(ExternalLaunch.Url(action.url)))
            is PlaceDetailAction.Call -> sendEvent(PlaceDetailEvent.Launch(ExternalLaunch.Dial(action.phone)))
            PlaceDetailAction.UserScrolled -> navAudio.notifyUserScrolled()
            PlaceDetailAction.Listen -> Unit // playback is owned by the orchestrator
        }
    }

    /** Idempotent; on save the title/photo/area are remembered first, for Saved offline. */
    private fun setSaved(saved: Boolean, place: Place?) {
        if (!saved) {
            favorites.remove(FavoriteItemType.PLACE, id)
            return
        }
        place?.let { p ->
            snapshots.putAll(mapOf(FavoriteKey(FavoriteItemType.PLACE, id) to p.toSavedSnapshot(areaKeyFor(p.zone))))
        }
        favorites.save(FavoriteItemType.PLACE, id)
    }

    private suspend fun load() {
        updateState { it.copy(isLoading = it.place == null, error = null) }
        getPlaceById(id)
            .onSuccess { place -> updateState { it.copy(isLoading = false, place = place, audioTrack = place.toAudioTrack()) } }
            .onFailure { updateState { it.copy(isLoading = false, error = if (it.place == null) LOAD_FAILED else null) } }
    }

    /** The app-language track, else English (flagged so the control can say "English audio"). */
    private fun Place.toAudioTrack(): AudioTrack? {
        val lang = appPreferences.getSelectedLanguage()
        return AudioTrack.of(AudioSource.PLACE, id, localizedName(lang), zone.orEmpty().prettyLabel(), audioFile, lang, audioGuideRepository::isAvailableOffline)
    }

    private companion object {
        val LOAD_FAILED = UiText { it.detail.placeLoadError }
    }
}
