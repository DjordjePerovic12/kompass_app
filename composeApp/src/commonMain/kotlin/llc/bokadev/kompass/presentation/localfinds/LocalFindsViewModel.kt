package llc.bokadev.kompass.presentation.localfinds

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.stories_load_error
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind
import llc.bokadev.kompass.presentation.localfinds.utils.cardLayoutFor
import llc.bokadev.kompass.presentation.localfinds.utils.readMinutes
import llc.bokadev.kompass.presentation.localfinds.utils.storiesInFilter
import llc.bokadev.kompass.presentation.localfinds.utils.storyArea
import llc.bokadev.kompass.presentation.localfinds.utils.storyExcerpt
import llc.bokadev.kompass.presentation.localfinds.utils.storyKind

class LocalFindsViewModel(
    private val getLocalFinds: GetLocalFindsUseCase,
    private val preferences: AppPreferences,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<LocalFindsState, LocalFindsEvent>() {

    override val initialViewState = LocalFindsState()
    private var hasLoadedInitialData = false
    private var stories: List<LocalFind> = emptyList()
    private var loadJob: Job? = null

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            preferences.selectedLanguageFlow.drop(1).collect { rebuild() }
        }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val localFindsState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("legends_localisms")
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: LocalFindsEvent) {
        when (event) {
            LocalFindsEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            LocalFindsEvent.OnRetryClick -> load()
            is LocalFindsEvent.OnFilterSelect -> {
                state.update { it.copy(filter = event.kind) }
                rebuild()
            }

            is LocalFindsEvent.OnStoryClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.LocalFindDetailScreen(event.id, state.value.filter?.name))
            }
        }
    }

    /** Keeps what's on screen while retrying; a newer load cancels an older one. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = stories.isEmpty(), error = null) }
            getLocalFinds()
                .onSuccess { items ->
                    stories = items.filter { it.isActive }.sortedBy { it.sortOrder }
                    state.update { it.copy(isLoading = false) }
                    rebuild()
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (stories.isEmpty()) BaseError.InlineError(Res.string.stories_load_error) else null
                        )
                    }
                }
        }
    }

    private fun rebuild() {
        val lang = preferences.getSelectedLanguage()
        val kinds = StoryKind.entries.filter { kind -> stories.any { it.storyKind == kind } }
        // A filter for a kind that no longer exists falls back to All.
        val filter = state.value.filter?.takeIf { it in kinds && kinds.size > 1 }
        val cards = storiesInFilter(stories, filter).mapIndexed { index, story -> story.toCard(index, lang) }
        state.update { it.copy(filter = filter, kinds = kinds, cards = cards) }
    }
}

internal fun LocalFind.toCard(index: Int, lang: String): StoryCard {
    val body = localizedBody(lang)
    val photo = photos.firstOrNull { it.isNotBlank() }
    return StoryCard(
        id = id,
        kind = storyKind,
        layout = cardLayoutFor(index, storyKind, photo != null),
        title = localizedTitle(lang),
        excerpt = storyExcerpt(body),
        area = storyArea(localizedLocation(lang)),
        photo = photo,
        readMinutes = readMinutes(body),
        hasAudio = !localizedAudioFile(lang).isNullOrBlank()
    )
}
