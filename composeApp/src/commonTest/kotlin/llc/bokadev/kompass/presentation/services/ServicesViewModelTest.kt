package llc.bokadev.kompass.presentation.services

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository
import llc.bokadev.kompass.domain.usecase.GetServicesUseCase
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.testutil.NoOpAnalytics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ServicesViewModelTest {

    @Test
    fun servicesFailureWithoutDataIsAnErrorNotAnEmptyList() = screen(fail = true) { vm, repo, _ ->
        assertNotNull(vm.servicesState.value.error)
        repo.fail = false
        vm.onEvent(ServicesEvent.OnRetryClick)
        runCurrent()
        assertNull(vm.servicesState.value.error, "a successful empty result is 'none listed', not an error")
        assertTrue(vm.servicesState.value.services.isEmpty())
    }

    @Test
    fun openingAServiceAndBackGoThroughTheNavigator() = screen { vm, _, navigator ->
        vm.onEvent(ServicesEvent.OnServiceClick("svc"))
        vm.onEvent(ServicesEvent.NavigateBack)
        runCurrent()
        assertEquals(listOf<Screen>(Screen.ServiceDetailScreen("svc")), navigator.destinations)
        assertEquals(NavType.NavigateUp, navigator.recorded.last())
    }

    private class FakeServices(var fail: Boolean) : ServiceRepository {
        override suspend fun getServices(): Resource<List<Service>, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.UNKNOWN, "x") else Resource.Success(emptyList())
        override suspend fun getServiceById(id: String): Resource<Service, NetworkError, String?> = error("unused")
    }

    /** Subscribes to the state (which starts the first load) and runs it before [block]. */
    private fun screen(
        fail: Boolean = false,
        block: suspend TestScope.(ServicesViewModel, FakeServices, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeServices(fail)
        val navigator = FakeNavigator()
        val vm = ServicesViewModel(GetServicesUseCase(repo), navigator, NoOpAnalytics())
        try {
            backgroundScope.launch { vm.servicesState.collect {} }
            runCurrent()
            block(vm, repo, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }
}
