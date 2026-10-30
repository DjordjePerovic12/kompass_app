package llc.bokadev.kompass.presentation.eventdetail

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.events_couldnt_load
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
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.repository.EventRepository
import llc.bokadev.kompass.domain.usecase.GetEventByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.testutil.FakeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest {

    @Test
    fun loadsTheEventOnFirstCollection() = screen { vm, _, _ ->
        vm.eventDetailState.test {
            skipItems(1)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("concert", state.event?.id)
        }
    }

    @Test
    fun failureWithNothingShownIsAnInlineErrorAndRetryRecovers() = screen(fail = true) { vm, repo, _ ->
        vm.eventDetailState.test {
            advanceUntilIdle()
            assertEquals(BaseError.InlineError(Res.string.events_couldnt_load), expectMostRecentItem().error)
            repo.fail = false
            vm.onEvent(EventDetailEvent.OnRetryClick)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.error)
            assertEquals("concert", state.event?.id)
        }
    }

    @Test
    fun backNavigatesUp() = screen { vm, _, navigator ->
        vm.onEvent(EventDetailEvent.NavigateBack)
        advanceUntilIdle()
        assertEquals(listOf<NavType>(NavType.NavigateUp), navigator.recorded)
    }

    @Test
    fun ticketsAndMapsHandOffThroughTheEventFlow() = screen(lang = "de") { vm, _, _ ->
        vm.eventDetailState.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        vm.eventFlow.test {
            vm.onEvent(EventDetailEvent.OnTicketsClick)
            assertEquals(EventDetailEvent.OpenExternal(ExternalLaunch.Url("https://tickets.example")), awaitItem())
            vm.onEvent(EventDetailEvent.OnMapsClick)
            assertEquals(EventDetailEvent.OpenExternal(ExternalLaunch.Maps(42.42, 18.77, "Altstadt")), awaitItem())
        }
    }

    private fun screen(
        fail: Boolean = false,
        lang: String = "en",
        block: suspend TestScope.(EventDetailViewModel, FakeEvents, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeEvents(fail)
        val navigator = FakeNavigator()
        val vm = EventDetailViewModel("concert", GetEventByIdUseCase(repo), navigator, Prefs(lang))
        try {
            block(vm, repo, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class FakeEvents(var fail: Boolean) : EventRepository {
        val concert = Event(
            id = "concert", name = mapOf("en" to "Concert"), description = emptyMap(),
            venue = mapOf("en" to "Old Town", "de" to "Altstadt"), latitude = 42.42, longitude = 18.77,
            category = "music", startTime = "2026-10-08T18:00:00Z", endTime = null, price = null,
            ticketUrl = "  https://tickets.example ", isRecurring = false, photos = emptyList()
        )

        override suspend fun getEvents(dateFilter: String, eventType: String): Resource<List<Event>, NetworkError, String?> = error("unused")
        override suspend fun getEventFilters(): Resource<List<EventFilter>, NetworkError, String?> = error("unused")
        override suspend fun getUpcomingEvents(): Resource<List<Event>, NetworkError, String?> = error("unused")
        override suspend fun getEventById(id: String): Resource<Event, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.NO_INTERNET, "offline") else Resource.Success(concert)
    }

    private class Prefs(lang: String) : AppPreferences {
        override val selectedLanguageFlow = MutableStateFlow(lang)
        override fun getString(key: String): String? = null
        override fun setString(key: String, value: String?) = Unit
        override fun getSelectedLanguage() = selectedLanguageFlow.value
        override fun setSelectedLanguage(language: String) { selectedLanguageFlow.value = language }
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
    }
}
