package llc.bokadev.kompass.presentation.excursions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_section_error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.data.remote.dto.ExcursionDto
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.ExcursionResult
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.excursions.related_activities.RelatedActivitiesEvent
import llc.bokadev.kompass.presentation.excursions.related_activities.RelatedActivitiesViewModel
import llc.bokadev.kompass.presentation.screens.favorites.FavoritesViewModelTest
import llc.bokadev.kompass.testutil.FakeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExcursionsViewModelTest {
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
        val first = ExcursionsViewModel(repo, saved, FavoritesViewModelTest.FakeFavorites(emptyList()), FakeNavigator())
        val filter = ExcursionFilter(setOf("culture", "water"), "half_day", "arrange_ahead")
        first.onEvent(ExcursionsEvent.OnFilterChange(filter))
        val restored = ExcursionsViewModel(repo, saved, FavoritesViewModelTest.FakeFavorites(emptyList()), FakeNavigator())
        try {
            restored.excursionsState.test {
                advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(filter, state.filter)
                assertTrue(state.cached)
                restored.onEvent(ExcursionsEvent.OnFilterChange(ExcursionFilter()))
                assertEquals(ExcursionFilter(), awaitItem().filter)
                assertNull(saved.get<String>("time"))
            }
        } finally {
            first.viewModelScope.cancel()
            restored.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    @Test fun errorsCanRetryWithoutLosingFilters() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var fail = true
        val repo = object : ExcursionRepository {
            override suspend fun getCollection() = if (fail) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(ExcursionResult(listOf(entry())))
            override suspend fun getRelatedActivities(placeId: String) = Resource.Success(ExcursionResult<Experience>(emptyList()))
        }
        val vm = ExcursionsViewModel(repo, SavedStateHandle(), FavoritesViewModelTest.FakeFavorites(emptyList()), FakeNavigator())
        try {
            vm.excursionsState.test {
                advanceUntilIdle()
                assertEquals(BaseError.InlineError(Res.string.redesign_section_error), expectMostRecentItem().error)
                vm.onEvent(ExcursionsEvent.OnFilterChange(ExcursionFilter(setOf("water"))))
                fail = false
                vm.onEvent(ExcursionsEvent.OnRetryClick)
                advanceUntilIdle()
                val state = expectMostRecentItem()
                assertNull(state.error)
                assertEquals(setOf("water"), state.filter.interests)
                assertEquals(1, state.items.size)
            }
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    @Test fun backSaveAndOpenGoThroughTheNavigatorAndFavorites() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val navigator = FakeNavigator()
        val vm = ExcursionsViewModel(fake(Resource.Success(ExcursionResult(listOf(entry())))), SavedStateHandle(), FavoritesViewModelTest.FakeFavorites(emptyList()), navigator)
        try {
            vm.excursionsState.test {
                vm.onEvent(ExcursionsEvent.OnSaveClick(FavoriteItemType.ACTIVITY, "islands", saved = true))
                advanceUntilIdle()
                assertTrue(FavoriteKey(FavoriteItemType.ACTIVITY, "islands") in expectMostRecentItem().savedKeys)
                vm.onEvent(ExcursionsEvent.OnSaveClick(FavoriteItemType.ACTIVITY, "islands", saved = false))
                advanceUntilIdle()
                assertTrue(expectMostRecentItem().savedKeys.isEmpty())
            }
            vm.onEvent(ExcursionsEvent.OnExcursionClick(FavoriteItemType.PLACE, "lovcen"))
            vm.onEvent(ExcursionsEvent.OnExcursionClick(FavoriteItemType.ACTIVITY, "islands"))
            vm.onEvent(ExcursionsEvent.NavigateBack)
            advanceUntilIdle()
            assertEquals(listOf<Screen>(Screen.PlaceDetailScreen("lovcen"), Screen.ExperienceDetailScreen("islands")), navigator.destinations)
            assertEquals(NavType.NavigateUp, navigator.recorded.last())
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    // Related activities (the place-detail section)

    @Test fun relatedActivitiesLoadForTheirOwnPlaceAndRetryAfterAFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val requested = mutableListOf<String>()
        var fail = true
        val repo = object : ExcursionRepository {
            override suspend fun getCollection() = Resource.Success(ExcursionResult<Excursion>(emptyList()))
            override suspend fun getRelatedActivities(placeId: String): Resource<ExcursionResult<Experience>, NetworkError, String?> {
                requested += placeId
                return if (fail) Resource.Error(NetworkError.NO_INTERNET, "offline")
                else Resource.Success(ExcursionResult(listOf(entry().activity!!), isCached = true))
            }
        }
        val vm = RelatedActivitiesViewModel("lovcen", repo, FavoritesViewModelTest.FakeFavorites(emptyList()), FakeNavigator())
        try {
            vm.relatedActivitiesState.test {
                advanceUntilIdle()
                val failed = expectMostRecentItem()
                assertFalse(failed.isLoading)
                assertEquals(BaseError.InlineError(Res.string.redesign_section_error), failed.error)
                fail = false
                vm.onEvent(RelatedActivitiesEvent.OnRetryClick)
                advanceUntilIdle()
                val state = expectMostRecentItem()
                assertNull(state.error)
                assertTrue(state.cached)
                assertEquals(listOf("islands"), state.items.map { it.id })
            }
            assertEquals(listOf("lovcen", "lovcen"), requested)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    @Test fun relatedActivitiesSaveAndOpenTheActivity() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val navigator = FakeNavigator()
        val favorites = FavoritesViewModelTest.FakeFavorites(emptyList())
        val repo = fake(Resource.Success(ExcursionResult(emptyList())))
        val vm = RelatedActivitiesViewModel("lovcen", repo, favorites, navigator)
        try {
            vm.relatedActivitiesState.test {
                advanceUntilIdle()
                // Nothing related and nothing failed: the section stays hidden.
                val empty = expectMostRecentItem()
                assertTrue(empty.items.isEmpty())
                assertNull(empty.error)
                vm.onEvent(RelatedActivitiesEvent.OnSaveClick("islands", saved = true))
                advanceUntilIdle()
                assertEquals(setOf("islands"), expectMostRecentItem().savedActivityIds)
                assertTrue(favorites.isFavorited(FavoriteItemType.ACTIVITY, "islands"))
                vm.onEvent(RelatedActivitiesEvent.OnSaveClick("islands", saved = false))
                advanceUntilIdle()
                assertTrue(expectMostRecentItem().savedActivityIds.isEmpty())
            }
            vm.onEvent(RelatedActivitiesEvent.OnActivityClick("islands"))
            advanceUntilIdle()
            assertEquals(listOf<Screen>(Screen.ExperienceDetailScreen("islands")), navigator.destinations)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private fun fake(result: Resource<ExcursionResult<Excursion>, NetworkError, String?>) = object : ExcursionRepository {
        override suspend fun getCollection() = result
        override suspend fun getRelatedActivities(placeId: String) = Resource.Success(ExcursionResult<Experience>(emptyList()))
    }
}
