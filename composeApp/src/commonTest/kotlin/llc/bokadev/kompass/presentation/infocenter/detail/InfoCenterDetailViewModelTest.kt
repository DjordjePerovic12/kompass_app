package llc.bokadev.kompass.presentation.infocenter.detail

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.InfoCenterRepository
import llc.bokadev.kompass.domain.usecase.GetInfoNoticeByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.testutil.FakeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InfoCenterDetailViewModelTest {

    @Test
    fun loadsTheNoticeOnFirstCollection() = screen { vm, _, _ ->
        vm.infoCenterDetailState.test {
            skipItems(1)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("water", state.notice?.id)
        }
    }

    @Test
    fun failureWithNothingShownIsAnInlineErrorAndRetryRecovers() = screen(fail = true) { vm, repo, _ ->
        vm.infoCenterDetailState.test {
            advanceUntilIdle()
            assertEquals(BaseError.InlineError(Res.string.states_couldnt_load_update), expectMostRecentItem().error)
            repo.fail = false
            vm.onEvent(InfoCenterDetailEvent.OnRetryClick)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.error)
            assertEquals("water", state.notice?.id)
        }
    }

    @Test
    fun backNavigatesUp() = screen { vm, _, navigator ->
        vm.onEvent(InfoCenterDetailEvent.NavigateBack)
        advanceUntilIdle()
        assertEquals(listOf<NavType>(NavType.NavigateUp), navigator.recorded)
    }

    @Test
    fun sourceHandsOffThroughTheEventFlow() = screen { vm, _, _ ->
        vm.infoCenterDetailState.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        vm.eventFlow.test {
            vm.onEvent(InfoCenterDetailEvent.OnSourceClick)
            assertEquals(InfoCenterDetailEvent.OpenExternal(ExternalLaunch.Url("https://kotor.me/water")), awaitItem())
        }
    }

    private fun screen(
        fail: Boolean = false,
        block: suspend TestScope.(InfoCenterDetailViewModel, FakeNotices, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeNotices(fail)
        val navigator = FakeNavigator()
        val vm = InfoCenterDetailViewModel("water", GetInfoNoticeByIdUseCase(repo), navigator)
        try {
            block(vm, repo, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private class FakeNotices(var fail: Boolean) : InfoCenterRepository {
        val water = InfoNotice(
            id = "water", cityId = "kotor", title = mapOf("en" to "Water supply interruption"),
            shortDescription = mapOf("en" to "No water in Dobrota this morning."), externalUrl = "  https://kotor.me/water "
        )

        override suspend fun getCurrentNotices(): Resource<List<InfoNotice>, NetworkError, String?> = error("unused")
        override suspend fun getNoticeById(id: String): Resource<InfoNotice, NetworkError, String?> =
            if (fail) Resource.Error(NetworkError.NO_INTERNET, "offline") else Resource.Success(water)
    }
}
