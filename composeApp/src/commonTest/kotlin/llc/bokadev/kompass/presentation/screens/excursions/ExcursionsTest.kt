package llc.bokadev.kompass.presentation.screens.excursions

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.presentation.screens.favorites.FavoritesViewModelTest
import llc.bokadev.kompass.testutil.RecordingNavigator
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.data.remote.dto.ExcursionDto
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExcursionsTest {
    private fun entry(id: String = "outing") = Json.decodeFromString<ExcursionDto>("""
        {"id":"$id","is_active":true,"interests":["culture","water"],"time_band":"half_day","planning_level":"arrange_ahead",
        "activity":{"id":"islands","city_id":"kotor","name":{"en":"Perast islands"},"description":{}}}
    """).toDomainOrNull()!!

    @Test fun interestsAreOrAndGroupsAreAnd() {
        val item = entry()
        assertTrue(ExcursionFilter(setOf("hiking", "culture"), "half_day", "arrange_ahead").matches(item))
        assertFalse(ExcursionFilter(setOf("hiking"), "half_day", "arrange_ahead").matches(item))
        assertFalse(ExcursionFilter(setOf("culture"), "short", "arrange_ahead").matches(item))
        assertFalse(ExcursionFilter(setOf("culture"), "half_day", "independent").matches(item))
    }

    @Test fun missingMetadataIsNotInvented() {
        val item = entry().copy(interests = emptySet(), timeBand = null, planningLevel = null)
        assertTrue(ExcursionFilter().matches(item))
        assertFalse(ExcursionFilter(setOf("nature")).matches(item))
        assertFalse(ExcursionFilter(timeBand = "short").matches(item))
        assertFalse(ExcursionFilter(planningLevel = "independent").matches(item))
    }

    @Test fun filteringPreservesEditorialOrder() {
        val items = listOf(entry("b"), entry("a"), entry("c"))
        assertEquals(listOf("b", "a", "c"), items.filter(ExcursionFilter(setOf("water"))::matches).map { it.id })
    }

    @Test fun standaloneActivityNeedsNoPlaceAndKeepsItsIdentity() {
        assertNull(entry().place)
        assertEquals("islands", entry().activity!!.id)
        assertFailsWith<IllegalArgumentException> { Excursion("invalid") }
    }

    @Test fun inactiveOrMissingTargetsAreOmitted() {
        val raw = """{"id":"a","is_active":true,"activity":{"id":"islands","city_id":"kotor","name":{},"description":{},"is_active":false}}"""
        assertNull(Json.decodeFromString<ExcursionDto>(raw).toDomainOrNull())
        assertNull(Json.decodeFromString<ExcursionDto>("""{"id":"missing","is_active":true}""").toDomainOrNull())
        assertNull(Json.decodeFromString<ExcursionDto>(raw.replace("\"is_active\":true", "\"is_active\":false")).toDomainOrNull())
    }

    @Test fun filtersRestoreAcrossViewModelRecreationAndReset() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val saved = SavedStateHandle()
        val repo = fake(Resource.Success(ExcursionResult(listOf(entry()), true)))
        val first = ExcursionsViewModel(repo, saved, FavoritesViewModelTest.FakeFavorites(emptyList()), RecordingNavigator())
        val filter = ExcursionFilter(setOf("culture", "water"), "half_day", "arrange_ahead")
        first.onAction(ExcursionsAction.Filter(filter))
        val restored = ExcursionsViewModel(repo, saved, FavoritesViewModelTest.FakeFavorites(emptyList()), RecordingNavigator())
        try {
            advanceUntilIdle()
            assertEquals(filter, restored.state.value.filter)
            assertTrue(restored.state.value.cached)
            restored.onAction(ExcursionsAction.Filter(ExcursionFilter()))
            assertEquals(ExcursionFilter(), restored.state.value.filter)
            assertNull(saved.get<String>("time"))
        } finally { first.viewModelScope.cancel(); restored.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test fun errorsCanRetryWithoutLosingFilters() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var fail = true
        val repo = object : ExcursionRepository {
            override suspend fun getCollection() = if (fail) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(ExcursionResult(listOf(entry())))
            override suspend fun getRelatedActivities(placeId: String) = Resource.Success(ExcursionResult<Experience>(emptyList()))
        }
        val vm = ExcursionsViewModel(repo, SavedStateHandle(), FavoritesViewModelTest.FakeFavorites(emptyList()), RecordingNavigator())
        try {
            advanceUntilIdle()
            assertNotNull(vm.state.value.error)
            vm.onAction(ExcursionsAction.Filter(ExcursionFilter(setOf("water"))))
            fail = false
            vm.onAction(ExcursionsAction.Reload)
            advanceUntilIdle()
            assertNull(vm.state.value.error)
            assertEquals(setOf("water"), vm.state.value.filter.interests)
            assertEquals(1, vm.state.value.items.size)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private fun fake(result: Resource<ExcursionResult<Excursion>, NetworkError, String?>) = object : ExcursionRepository {
        override suspend fun getCollection() = result
        override suspend fun getRelatedActivities(placeId: String) = Resource.Success(ExcursionResult<Experience>(emptyList()))
    }
}
