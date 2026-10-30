package llc.bokadev.kompass.presentation.screens.localfinds

import kotlinx.coroutines.flow.drop
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.navigation.Route
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase

class LocalFindsViewModel(
    private val getLocalFinds: GetLocalFindsUseCase,
    private val preferences: AppPreferences,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<LocalFindsState, LocalFindsAction, Nothing>(LocalFindsState()) {

    private var stories: List<LocalFind> = emptyList()
    private var loadJob: Job? = null

    init {
        // A language change elsewhere (tab header) must re-localize what this screen built from it.
        viewModelScope.launch {
            preferences.selectedLanguageFlow.drop(1).collect { rebuild() }
        }
    }

    override suspend fun onFirstStart() {
        analytics.trackScreenView("legends_localisms")
        load()
    }

    override fun onAction(action: LocalFindsAction) {
        when (action) {
            LocalFindsAction.Load -> load()
            is LocalFindsAction.SelectFilter -> {
                updateState { it.copy(filter = action.kind) }
                rebuild()
            }
            is LocalFindsAction.Open -> navigator.navigateTo(Route.LocalFindDetail(action.id, state.value.filter?.name))
            LocalFindsAction.Back -> navigator.navigateUp()
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = stories.isEmpty(), error = null) }
            getLocalFinds()
                .onSuccess { items ->
                    stories = items.filter { it.isActive }.sortedBy { it.sortOrder }
                    updateState { it.copy(isLoading = false) }
                    rebuild()
                }
                .onFailure {
                    updateState { it.copy(isLoading = false, error = if (stories.isEmpty()) UiText { s -> s.stories.loadError } else null) }
                }
        }
    }

    private fun rebuild() {
        val lang = preferences.getSelectedLanguage()
        val kinds = StoryKind.entries.filter { kind -> stories.any { it.storyKind == kind } }
        // A filter for a kind that no longer exists falls back to All.
        val filter = state.value.filter?.takeIf { it in kinds && kinds.size > 1 }
        val cards = storiesInFilter(stories, filter).mapIndexed { index, story -> story.toCard(index, lang) }
        updateState { it.copy(filter = filter, kinds = kinds, cards = cards) }
    }
}

/** Editorial order, narrowed to [filter] (null = all). Shared with the detail's "Next story". */
internal fun storiesInFilter(stories: List<LocalFind>, filter: StoryKind?): List<LocalFind> =
    stories.filter { filter == null || it.storyKind == filter }

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
