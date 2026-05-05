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
    data object Events : Route

    @Serializable
    data class ExperienceDetail(val id: String) : Route

    @Serializable
    data class ExperienceGuide(val id: String) : Route

    @Serializable
    data class ItineraryDetail(val id: String) : Route

    @Serializable
    data class CategoryItemsList(val category: String) : Route

    @Serializable
    data object NearbyPlaces : Route

    @Serializable
    data object Activities : Route

    @Serializable
    data object Essentials : Route

    @Serializable
    data object Services : Route

    @Serializable
    data object MyGuides : Route

    @Serializable
    data object InfoCenter : Route

    @Serializable
    data class InfoCenterDetail(val id: String) : Route

    @Serializable
    data class PremiumBundles(val activityId: String? = null) : Route

    @Serializable
    data class PaymentCheckout(val sessionId: String) : Route
}
