package llc.bokadev.kompass.presentation.screens.favorites

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class FavoritesState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val items: List<FavoriteDisplayItem> = emptyList()
) : BaseState()

sealed interface FavoritesEvent : BaseEvent {
    data object Load : FavoritesEvent
}

class FavoritesViewModel(
    private val placeRepository: PlaceRepository,
    private val getActivities: GetActivitiesUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val userLocationProvider: UserLocationProvider
) : BaseViewModel<FavoritesState, FavoritesEvent>() {

    override val initialState = FavoritesState()

    init {
        viewModelScope.launch {
            favoritesRepository.favoritesFlow.collect { load() }
        }
        load()
    }

    override fun onIntent(event: FavoritesEvent) {
        when (event) {
            FavoritesEvent.Load -> load()
        }
    }

    fun toggleFavorite(type: FavoriteItemType, id: String) {
        favoritesRepository.toggleFavorite(type, id)
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val placesDeferred = async { placeRepository.getActivePlaces() }
            val activitiesDeferred = async { getActivities() }

            val placesResult = placesDeferred.await()
            val activitiesResult = activitiesDeferred.await()

            val places = placesResult.getOrDefault(emptyList())
            val activities = activitiesResult.getOrDefault(emptyList())
            val favorites = favoritesRepository.favoritesFlow.value
            val distanceOrigin = userLocationProvider.getCurrentLocation() ?: KOTOR_OLD_TOWN_CENTER

            val items = favorites.mapNotNull { favorite ->
                when (favorite.type) {
                    FavoriteItemType.PLACE -> {
                        places.firstOrNull { it.id == favorite.id }?.let { place ->
                            FavoriteDisplayItem.PlaceItem(
                                place = place,
                                favoritedAtEpochMs = favorite.favoritedAtEpochMs,
                                distanceKm = haversineDistanceKm(distanceOrigin, GeoPoint(place.latitude, place.longitude))
                            )
                        }
                    }
                    FavoriteItemType.ACTIVITY -> {
                        activities.firstOrNull { it.id == favorite.id }?.let { activity ->
                            FavoriteDisplayItem.ActivityItem(
                                activity = activity,
                                favoritedAtEpochMs = favorite.favoritedAtEpochMs,
                                distanceKm = activity.latitude?.let { lat ->
                                    activity.longitude?.let { lon ->
                                        haversineDistanceKm(distanceOrigin, GeoPoint(lat, lon))
                                    }
                                }
                            )
                        }
                    }
                }
            }.sortedWith(
                compareBy<FavoriteDisplayItem> { item ->
                    when (item) {
                        is FavoriteDisplayItem.PlaceItem -> if (item.distanceKm != null) 0 else 1
                        is FavoriteDisplayItem.ActivityItem -> if (item.distanceKm != null) 0 else 1
                    }
                }.thenBy { item ->
                    when (item) {
                        is FavoriteDisplayItem.PlaceItem -> item.distanceKm ?: Double.MAX_VALUE
                        is FavoriteDisplayItem.ActivityItem -> item.distanceKm ?: Double.MAX_VALUE
                    }
                }.thenByDescending { item ->
                    when (item) {
                        is FavoriteDisplayItem.PlaceItem -> item.favoritedAtEpochMs
                        is FavoriteDisplayItem.ActivityItem -> item.favoritedAtEpochMs
                    }
                }
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (items.isEmpty() && (placesResult.isFailure || activitiesResult.isFailure)) {
                        placesResult.exceptionOrNull()?.message ?: activitiesResult.exceptionOrNull()?.message
                    } else null,
                    items = items
                )
            }
        }
    }

    private fun haversineDistanceKm(from: GeoPoint, to: GeoPoint): Double {
        val earthRadiusKm = 6371.0
        val dLat = (to.latitude - from.latitude).toRadians()
        val dLon = (to.longitude - from.longitude).toRadians()
        val fromLat = from.latitude.toRadians()
        val toLat = to.latitude.toRadians()

        val a = sin(dLat / 2).pow(2) +
            cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }

    private fun Double.toRadians(): Double = this * (kotlin.math.PI / 180.0)

    private companion object {
        val KOTOR_OLD_TOWN_CENTER = GeoPoint(
            latitude = 42.4246,
            longitude = 18.7712
        )
    }
}
