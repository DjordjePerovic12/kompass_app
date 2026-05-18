package llc.bokadev.kompass.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object LanguagePicker : Route

    @Serializable
    data class Main(
        val tab: String = BottomTab.Home.name,
        val backTab: String? = null
    ) : Route

    @Serializable
    data object ChangeLanguage : Route

    @Serializable
    data class PlaceDetail(val id: String) : Route

    @Serializable
    data class PlaceGuide(val id: String, val autoplay: Boolean = false, val deep: Boolean = false) : Route

    @Serializable
    data class EventDetail(val id: String) : Route

    @Serializable
    data object Events : Route

    @Serializable
    data class ExperienceDetail(val id: String) : Route

    @Serializable
    data class ExperienceGuide(val id: String, val autoplay: Boolean = false, val deep: Boolean = false) : Route

    @Serializable
    data class ItineraryDetail(val id: String) : Route

    @Serializable
    data class CategoryItemsList(val category: String) : Route

    @Serializable
    data object NearbyPlaces : Route

    @Serializable
    data object MustSeePlaces : Route

    @Serializable
    data object Activities : Route

    @Serializable
    data object Essentials : Route

    @Serializable
    data object Services : Route

    @Serializable
    data class ServiceDetail(val id: String) : Route

    @Serializable
    data object MyGuides : Route

    @Serializable
    data object Favorites : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object InfoCenter : Route

    @Serializable
    data class InfoCenterDetail(val id: String) : Route

    @Serializable
    data class PremiumBundles(val activityId: String? = null) : Route

    @Serializable
    data class PaymentCheckout(val sessionId: String) : Route

    @Serializable
    data object LocalFinds : Route

    @Serializable
    data class LocalFindDetail(val id: String) : Route
}
