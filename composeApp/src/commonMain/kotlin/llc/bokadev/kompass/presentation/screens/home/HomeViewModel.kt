package llc.bokadev.kompass.presentation.screens.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.GetNearbyPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetUpcomingEventsUseCase

class HomeViewModel(
    private val getMustSeePlaces: GetMustSeePlacesUseCase,
    private val getUpcomingEvents: GetUpcomingEventsUseCase,
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase,
    private val getNearbyPlaces: GetNearbyPlacesUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val premiumRepository: PremiumRepository
) : BaseViewModel<HomeState, HomeEvent>() {

    override val initialState = HomeState()

    init {
        loadHomeData()
    }

    override fun onIntent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadHomeData -> loadHomeData()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val entitlements = premiumRepository.getEntitlements()
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    hasPremiumAccess = entitlements.audioPass ||
                        entitlements.explorerPass ||
                        entitlements.perksPass
                )
            }

            val placesResult = async { getMustSeePlaces() }
            val eventsResult = async { getUpcomingEvents() }
            val noticesResult = async { getCurrentInfoNotices() }
            val nearbyResult = async { loadNearbyPlaces() }

            placesResult.await()
                .onSuccess { places -> _state.update { it.copy(mustSeePlaces = places) } }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                    return@launch
                }

            eventsResult.await()
                .onSuccess { events -> _state.update { it.copy(upcomingEvents = events) } }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                    return@launch
                }

            noticesResult.await()
                .onSuccess { notices ->
                    _state.update { it.copy(infoCenterNotices = notices.take(4)) }
                }
                .onFailure { err ->
                    _state.update { it.copy(infoCenterNotices = emptyList(), error = it.error ?: err.message) }
                }

            nearbyResult.await()
                .onSuccess { (nearbyPlaces, isUsingCurrentLocation) ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            nearbyPlaces = nearbyPlaces,
                            isUsingCurrentLocation = isUsingCurrentLocation
                        )
                    }
                }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }

    private suspend fun loadNearbyPlaces(): Result<Pair<List<NearbyPlace>, Boolean>> {
        val currentLocation = userLocationProvider.getCurrentLocation()
        val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

        return getNearbyPlaces(origin).mapCatching { nearbyPlaces ->
            nearbyPlaces
                .filter { nearbyPlace ->
                    nearbyPlace.distanceKm <= 3.5 &&
                        nearbyPlace.place.category == llc.bokadev.kompass.domain.model.PlaceCategory.SEE_AND_VISIT
                }
                .take(4) to (currentLocation != null)
        }
    }

    private companion object {
        val KOTOR_OLD_TOWN_CENTER = GeoPoint(
            latitude = 42.4246,
            longitude = 18.7712
        )
    }
}
