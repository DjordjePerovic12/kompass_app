package llc.bokadev.kompass.presentation.infocenter

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
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.InfoCenterRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.testutil.NoOpAnalytics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InfoCenterViewModelTest {

    @Test
    fun noticesKeepContentOnFailedRetryAndRecover() = screen { vm, repo, _ ->
        repo.fail = true
        vm.onEvent(InfoCenterEvent.OnRetryClick)
        runCurrent()
        assertTrue(vm.infoCenterState.value.refreshFailed)
        assertEquals(1, vm.infoCenterState.value.notices.size)
        assertNull(vm.infoCenterState.value.error)
    }

    @Test
    fun openingANoticeAndBackGoThroughTheNavigator() = screen { vm, _, navigator ->
        vm.onEvent(InfoCenterEvent.OnNoticeClick("n"))
        vm.onEvent(InfoCenterEvent.NavigateBack)
        runCurrent()
        assertEquals(listOf<Screen>(Screen.InfoCenterDetailScreen("n")), navigator.destinations)
        assertEquals(NavType.NavigateUp, navigator.recorded.last())
    }

    private class FakeNotices(var fail: Boolean) : InfoCenterRepository {
        override suspend fun getCurrentNotices(): Resource<List<InfoNotice>, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.UNKNOWN, "x") else Resource.Success(listOf(InfoNotice("n", "kotor", mapOf("en" to "Road closed"), emptyMap())))
        override suspend fun getNoticeById(id: String): Resource<InfoNotice, NetworkError, String?> = error("unused")
    }

    /** Subscribes to the state (which starts the first load) and runs it before [block]. */
    private fun screen(block: suspend TestScope.(InfoCenterViewModel, FakeNotices, FakeNavigator) -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeNotices(fail = false)
        val navigator = FakeNavigator()
        val vm = InfoCenterViewModel(GetCurrentInfoNoticesUseCase(repo), navigator, NoOpAnalytics())
        try {
            backgroundScope.launch { vm.infoCenterState.collect {} }
            runCurrent()
            block(vm, repo, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }
}
