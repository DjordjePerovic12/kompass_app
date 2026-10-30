package llc.bokadev.kompass.presentation.localfinds.detail

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.stories_story_load_error
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.usecase.GetLocalFindByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind
import llc.bokadev.kompass.presentation.localfinds.utils.storiesInFilter
import llc.bokadev.kompass.presentation.localfinds.utils.storyArea
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class LocalFindDetailViewModel(
    /** The story id; the screen also keys its audio ownership on it. */
    val id: String,
    /** A [StoryKind] name, or null for All. */
    private val filter: String?,
    private val getLocalFindById: GetLocalFindByIdUseCase,
    private val getLocalFinds: GetLocalFindsUseCase,
    private val audioGuideRepository: AudioGuideRepository,
    private val preferences: AppPreferences,
    private val navigator: Navigator,
    private val navAudio: NavAudioHost
) : BaseViewModel<LocalFindDetailState, LocalFindDetailEvent>() {

    override val initialViewState = LocalFindDetailState()
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

    val localFindDetailState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: LocalFindDetailEvent) {
        when (event) {
            LocalFindDetailEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            LocalFindDetailEvent.OnRetryClick -> load()
            is LocalFindDetailEvent.OnMapClick ->
                openExternal(ExternalLaunch.Maps(event.latitude, event.longitude, event.title))

            is LocalFindDetailEvent.OnNextStoryClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.LocalFindDetailScreen(event.id, filter)) {
                    popUpTo<Screen.LocalFindDetailScreen> { inclusive = true }
                }
            }

            LocalFindDetailEvent.OnUserScrolled -> navAudio.notifyUserScrolled()
            is LocalFindDetailEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(LocalFindDetailEvent.OpenExternal(launch))
        }
    }

    private var loadJob: Job? = null

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = it.find == null, error = null) }
            val lang = preferences.getSelectedLanguage()
            getLocalFindById(id)
                .onSuccess { find ->
                    state.update { it.copy(isLoading = false, find = find, audioTrack = find.toAudioTrack(lang)) }
                    // "Next story" is optional: a failed list load just hides the card.
                    getLocalFinds().onSuccess { all ->
                        val kind = filter?.let { f -> StoryKind.entries.firstOrNull { it.name == f } }
                        state.update { it.copy(next = nextStory(all, id, kind, lang)) }
                    }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.find == null) BaseError.InlineError(Res.string.stories_story_load_error) else null
                        )
                    }
                }
        }
    }

    private fun LocalFind.toAudioTrack(lang: String): AudioTrack? =
        AudioTrack.of(AudioSource.STORY, id, localizedTitle(lang), storyArea(localizedLocation(lang)).orEmpty(), audioFile, lang, audioGuideRepository::isAvailableOffline)
}

/** The story after [id] in the editorial order of [filter]; null at the end or when [id] isn't in it. */
internal fun nextStory(all: List<LocalFind>, id: String, filter: StoryKind?, lang: String): NextStory? {
    val list = storiesInFilter(all.filter { it.isActive }.sortedBy { it.sortOrder }, filter)
    val index = list.indexOfFirst { it.id == id }
    if (index < 0) return null
    return list.getOrNull(index + 1)?.let { NextStory(it.id, it.localizedTitle(lang)) }
}
