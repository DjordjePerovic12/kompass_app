package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.domain.model.AudioSource
import kotlinx.coroutines.flow.drop
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.presentation.navigation.Route
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.usecase.GetLocalFindByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase

class LocalFindDetailViewModel(
    private val id: String,
    /** A [StoryKind] name, or null for All. */
    private val filter: String?,
    private val getLocalFindById: GetLocalFindByIdUseCase,
    private val getLocalFinds: GetLocalFindsUseCase,
    private val audioGuideRepository: AudioGuideRepository,
    private val preferences: AppPreferences,
    private val navigator: Navigator,
    private val navAudio: NavAudioHost
) : BaseViewModel<LocalFindDetailState, LocalFindDetailAction, LocalFindDetailEvent>(LocalFindDetailState()) {

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            preferences.selectedLanguageFlow.drop(1).collect { load() }
        }
    }

    override suspend fun onFirstStart() = load()

    override fun onAction(action: LocalFindDetailAction) {
        when (action) {
            LocalFindDetailAction.Retry -> load()
            LocalFindDetailAction.Back -> navigator.navigateUp()
            // Audio playback is tied to the screen's lifetime; its orchestrator handles Listen.
            LocalFindDetailAction.Listen -> Unit
            is LocalFindDetailAction.OpenMap ->
                sendEvent(LocalFindDetailEvent.Launch(ExternalLaunch.Maps(action.latitude, action.longitude, action.title)))
            is LocalFindDetailAction.OpenNext -> navigator.navigateTo(
                Route.LocalFindDetail(action.id, filter),
                popUpToRoute = Route.LocalFindDetail(id, filter),
                popUpToInclusive = true
            )
            LocalFindDetailAction.UserScrolled -> navAudio.notifyUserScrolled()
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = it.find == null, error = null) }
            val lang = preferences.getSelectedLanguage()
            getLocalFindById(id)
                .onSuccess { find ->
                    updateState { it.copy(isLoading = false, find = find, audioTrack = find.toAudioTrack(lang)) }
                    // "Next story" is optional: a failed list load just hides the card.
                    getLocalFinds().onSuccess { all ->
                        val kind = filter?.let { f -> StoryKind.entries.firstOrNull { it.name == f } }
                        updateState { it.copy(next = nextStory(all, id, kind, lang)) }
                    }
                }
                .onFailure { updateState { it.copy(isLoading = false, error = if (it.find == null) UiText { s -> s.stories.storyLoadError } else null) } }
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
