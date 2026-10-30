package llc.bokadev.kompass.presentation.localfinds

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.LocalFindRepository
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.presentation.localfinds.detail.nextStory
import llc.bokadev.kompass.presentation.localfinds.utils.StoryCardLayout
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind
import llc.bokadev.kompass.presentation.localfinds.utils.cardLayoutFor
import llc.bokadev.kompass.presentation.localfinds.utils.cleanStoryLocation
import llc.bokadev.kompass.presentation.localfinds.utils.pullQuoteOf
import llc.bokadev.kompass.presentation.localfinds.utils.readMinutes
import llc.bokadev.kompass.presentation.localfinds.utils.storyArea
import llc.bokadev.kompass.presentation.localfinds.utils.storyExcerpt
import llc.bokadev.kompass.presentation.localfinds.utils.storyParagraphs
import llc.bokadev.kompass.presentation.localfinds.utils.storyPlaceName
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.testutil.NoOpAnalytics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocalFindsViewModelTest {

    private val alkima = story(
        "alkima", order = 0, photo = "alkima.jpg", audio = true,
        body = "Long before the first stone was laid, a wise fairy named Alkima warned the founders. They did not listen.\n\nSecond paragraph.",
        location = "Old Town Kotor — Karampana Wall & Fairy's Gate"
    )
    private val rocks = story("rocks", order = 1, photo = "rocks.jpg", body = "Sailors added a stone every time they came home safe.", location = "Perast")
    private val noPhoto = story("polako", order = 2, photo = null, body = "Polako means slowly.", location = "")
    private val hidden = story("hidden", order = 3, photo = null, body = "x", location = "", active = false)

    @Test fun readTimeIsWordsOver200RoundedUpAndHiddenWhenEmpty() {
        assertEquals(1, readMinutes("one two three"))
        assertEquals(2, readMinutes(List(201) { "w" }.joinToString(" ")))
        assertNull(readMinutes("   "))
    }

    @Test fun excerptIsTheFirstSentenceNeverGenerated() {
        assertEquals("Long before the first stone was laid, a wise fairy named Alkima warned the founders.", storyExcerpt(alkima.body.getValue("en")))
        assertEquals("No full stop here", storyExcerpt("No full stop here\n\nNext"))
        assertNull(storyExcerpt(""))
    }

    @Test fun locationTextBecomesAreaCleanTextAndPlaceName() {
        val location = "Old Town Kotor — Karampana Wall & Fairy's Gate"
        assertEquals("Old Town Kotor", storyArea(location))
        assertEquals("Old Town Kotor: Karampana Wall & Fairy's Gate", cleanStoryLocation(location))
        assertEquals("Karampana Wall & Fairy's Gate", storyPlaceName(location))
        assertNull(storyPlaceName("Old Town Kotor — Karampana Wall and the Fairy's Gate near Vilinic"))
        assertNull(storyArea("A very long location sentence that is not an area name at all"))
        assertNull(cleanStoryLocation(""))
    }

    @Test fun cardShapeFollowsContent() {
        assertEquals(StoryCardLayout.FEATURE, cardLayoutFor(0, StoryKind.LEGEND, hasPhoto = false))
        assertEquals(StoryCardLayout.COMPACT, cardLayoutFor(1, StoryKind.LEGEND, hasPhoto = true))
        assertEquals(StoryCardLayout.QUOTE, cardLayoutFor(2, StoryKind.LEGEND, hasPhoto = false))
        assertEquals(StoryCardLayout.QUOTE, cardLayoutFor(3, StoryKind.LOCALISM, hasPhoto = true))
    }

    @Test fun pullQuotesAreMarkedParagraphs() {
        assertEquals("A short line", pullQuoteOf("> A short line"))
        assertNull(pullQuoteOf("A normal paragraph"))
        assertEquals(listOf("One", "> Two", "Three"), storyParagraphs("One\n\n> Two\nThree"))
        // Hard-wrapped mid-sentence lines join; a line ending a sentence breaks.
        assertEquals(listOf("A mountain with no water and no salt.", "The city rose."), storyParagraphs("A mountain with no water and\nno salt.\nThe city rose."))
    }

    @Test fun listBuildsCardsInEditorialOrderWithoutAFilterControlForOneKind() = screen(listOf(noPhoto, rocks, alkima, hidden)) { vm, _ ->
        vm.localFindsState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(listOf("alkima", "rocks", "polako"), state.cards.map { it.id })
            assertEquals(listOf(StoryCardLayout.FEATURE, StoryCardLayout.COMPACT, StoryCardLayout.QUOTE), state.cards.map { it.layout })
            assertEquals("Old Town Kotor", state.cards[0].area)
            assertTrue(state.cards[0].hasAudio)
            assertEquals(listOf(StoryKind.LEGEND), state.kinds)
        }
    }

    @Test fun nextStoryFollowsTheListAndEndsAtTheLast() {
        val all = listOf(noPhoto, alkima, rocks, hidden)
        assertEquals("rocks", nextStory(all, "alkima", null, "en")?.id)
        assertEquals("polako", nextStory(all, "rocks", null, "en")?.id)
        assertNull(nextStory(all, "polako", null, "en"))
        assertNull(nextStory(all, "missing", null, "en"))
    }

    @Test fun openingAStoryCarriesTheFilterAndBackNavigatesUp() = screen(listOf(alkima)) { vm, navigator ->
        vm.onEvent(LocalFindsEvent.OnStoryClick("alkima"))
        vm.onEvent(LocalFindsEvent.NavigateBack)
        advanceUntilIdle()
        assertEquals(listOf<Screen>(Screen.LocalFindDetailScreen("alkima", null)), navigator.destinations)
        assertEquals(NavType.NavigateUp, navigator.recorded.last())
    }

    private fun screen(
        items: List<LocalFind>,
        block: suspend TestScope.(LocalFindsViewModel, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val navigator = FakeNavigator()
        val vm = LocalFindsViewModel(GetLocalFindsUseCase(repo(items)), Prefs(), navigator, NoOpAnalytics())
        try {
            block(vm, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private fun repo(items: List<LocalFind>) = object : LocalFindRepository {
        override suspend fun getLocalFinds() = Resource.Success(items)
        override suspend fun getLocalFindById(id: String) = items.firstOrNull { it.id == id }?.let { Resource.Success(it) } ?: Resource.Error(NetworkError.NOT_FOUND, id)
    }

    private fun story(id: String, order: Int, photo: String?, body: String, location: String, audio: Boolean = false, active: Boolean = true) = LocalFind(
        id = id, cityId = "kotor", title = mapOf("en" to id.replaceFirstChar { it.uppercase() }), body = mapOf("en" to body),
        audioFile = if (audio) mapOf("en" to "$id.mp3") else null, location = mapOf("en" to location),
        latitude = null, longitude = null, photos = listOfNotNull(photo), isActive = active, sortOrder = order
    )

    private class Prefs : AppPreferences {
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String): String? = null
        override fun setString(key: String, value: String?) = Unit
        override fun getSelectedLanguage() = "en"
        override fun setSelectedLanguage(language: String) = Unit
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
    }
}
