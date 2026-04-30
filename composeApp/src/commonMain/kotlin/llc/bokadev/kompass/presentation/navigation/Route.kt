package llc.bokadev.kompass.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object LanguagePicker : Route

    @Serializable
    data object Main : Route

    @Serializable
    data class PlaceDetail(val id: String) : Route

    @Serializable
    data class EventDetail(val id: String) : Route

    @Serializable
    data class ExperienceDetail(val id: String) : Route

    @Serializable
    data class ItineraryDetail(val id: String) : Route

    @Serializable
    data class CategoryItemsList(val category: String) : Route

    @Serializable
    data object NearbyPlaces : Route

    @Serializable
    data object Essentials : Route

    @Serializable
    data object Services : Route
}
