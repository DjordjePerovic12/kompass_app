package llc.bokadev.kompass.presentation.placedetail

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_place_load_error
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
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetPlaceByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.presentation.shared.areaKeyFor
import llc.bokadev.kompass.presentation.shared.detail.prettyLabel

class PlaceDetailViewModel(
    /** The place id; the screen also keys its audio ownership and related activities on it. */
    val id: String,
    private val getPlaceById: GetPlaceByIdUseCase,
    private val audioGuideRepository: AudioGuideRepository,
    private val appPreferences: AppPreferences,
    private val favorites: FavoritesRepository,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    private val navAudio: NavAudioHost
) : BaseViewModel<PlaceDetailState, PlaceDetailEvent>() {

    override val initialViewState = PlaceDetailState()
    private var hasLoadedInitialData = false

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            appPreferences.selectedLanguageFlow.drop(1).collect {
                state.value.place?.let { p -> state.update { it.copy(audioTrack = p.toAudioTrack()) } }
            }
        }
        viewModelScope.launch {
            favorites.favoritesFlow.collect { list ->
                state.update { it.copy(isSaved = list.any { e -> e.type == FavoriteItemType.PLACE && e.id == id }) }
            }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val placeDetailState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadPlace()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: PlaceDetailEvent) {
        when (event) {
            PlaceDetailEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            PlaceDetailEvent.OnRetryClick -> loadPlace()
            is PlaceDetailEvent.OnSavedChange -> setSaved(event.saved, state.value.place)
            PlaceDetailEvent.OnMapsClick -> {
                val place = state.value.place ?: return
                openExternal(ExternalLaunch.Maps(place.latitude, place.longitude, place.localizedName(appPreferences.getSelectedLanguage())))
            }

            is PlaceDetailEvent.OnMenuClick -> openExternal(ExternalLaunch.Url(event.url))
            is PlaceDetailEvent.OnCallClick -> openExternal(ExternalLaunch.Dial(event.phone))
            PlaceDetailEvent.OnUserScrolled -> navAudio.notifyUserScrolled()
            is PlaceDetailEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(PlaceDetailEvent.OpenExternal(launch))
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

    private fun loadPlace() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = it.place == null, error = null) }
            getPlaceById(id)
                .onSuccess { place ->
                    state.update { it.copy(isLoading = false, place = place, audioTrack = place.toAudioTrack()) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.place == null) BaseError.InlineError(Res.string.detail_place_load_error) else null
                        )
                    }
                }
        }
    }

    /** The app-language track, else English (flagged so the control can say "English audio"). */
    private fun Place.toAudioTrack(): AudioTrack? {
        val lang = appPreferences.getSelectedLanguage()
        return AudioTrack.of(AudioSource.PLACE, id, localizedName(lang), zone.orEmpty().prettyLabel(), audioFile, lang, audioGuideRepository::isAvailableOffline)
    }
}
