package llc.bokadev.kompass.presentation.services.detail

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_service
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
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository
import llc.bokadev.kompass.domain.usecase.GetServiceByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.testutil.FakeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceDetailViewModelTest {

    @Test
    fun loadsTheServiceOnFirstCollection() = screen { vm, _, _ ->
        vm.serviceDetailState.test {
            skipItems(1)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("laundry", state.service?.id)
        }
    }

    @Test
    fun failureWithNothingShownIsAnInlineErrorAndRetryRecovers() = screen(fail = true) { vm, repo, _ ->
        vm.serviceDetailState.test {
            advanceUntilIdle()
            assertEquals(BaseError.InlineError(Res.string.states_couldnt_load_service), expectMostRecentItem().error)
            repo.fail = false
            vm.onEvent(ServiceDetailEvent.OnRetryClick)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.error)
            assertEquals("laundry", state.service?.id)
        }
    }

    @Test
    fun backNavigatesUp() = screen { vm, _, navigator ->
        vm.onEvent(ServiceDetailEvent.NavigateBack)
        advanceUntilIdle()
        assertEquals(listOf<NavType>(NavType.NavigateUp), navigator.recorded)
    }

    @Test
    fun websiteAndMapsHandOffThroughTheEventFlow() = screen(lang = "de") { vm, _, _ ->
        vm.serviceDetailState.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        vm.eventFlow.test {
            vm.onEvent(ServiceDetailEvent.OnWebsiteClick)
            assertEquals(ServiceDetailEvent.OpenExternal(ExternalLaunch.Url("https://baylaundry.me")), awaitItem())
            vm.onEvent(ServiceDetailEvent.OnMapsClick)
            assertEquals(ServiceDetailEvent.OpenExternal(ExternalLaunch.Maps(42.44, 18.76, "Buchtwäscherei")), awaitItem())
        }
    }

    private fun screen(
        fail: Boolean = false,
        lang: String = "en",
        block: suspend TestScope.(ServiceDetailViewModel, FakeServices, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeServices(fail)
        val navigator = FakeNavigator()
        val vm = ServiceDetailViewModel("laundry", GetServiceByIdUseCase(repo), navigator, Prefs(lang))
        try {
            block(vm, repo, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class FakeServices(var fail: Boolean) : ServiceRepository {
        val laundry = Service(
            id = "laundry", name = mapOf("en" to "Bay Laundry", "de" to "Buchtwäscherei"), description = emptyMap(),
            externalWebsite = " https://baylaundry.me  ", latitude = 42.44, longitude = 18.76
        )

        override suspend fun getServices(): Resource<List<Service>, NetworkError, String?> = error("unused")
        override suspend fun getServiceById(id: String): Resource<Service, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.NO_INTERNET, "offline") else Resource.Success(laundry)
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
        override fun hasAudioPass() = false
        override fun setAudioPass(value: Boolean) = Unit
        override fun hasExplorerPass() = false
        override fun setExplorerPass(value: Boolean) = Unit
        override fun hasPerksPass() = false
        override fun setPerksPass(value: Boolean) = Unit
    }
}
