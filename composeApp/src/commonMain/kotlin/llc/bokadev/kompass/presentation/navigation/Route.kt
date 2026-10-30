package llc.bokadev.kompass.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object LanguagePicker : Route

    @Serializable
    data object Main : Route

    @Serializable
    data class TabGraph(val tab: String = BottomTab.Home.name) : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Explore : Route

    /** [autoplay] starts the place's audio once it has loaded (Home "Listen"). */
    @Serializable
    data class PlaceDetail(val id: String, val autoplay: Boolean = false) : Route

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

    /**
     * The shared Lamplight list. [kind] is a CategoryListKind name; [area] (a HomeArea preference
     * value such as "perast") pre-applies the area filter, e.g. from Home's manual-area "See all".
     */
    @Serializable
    data class CategoryList(val kind: String, val area: String? = null) : Route

    @Serializable
    data class NearbyPlaces(val worthTheDrive: Boolean = false) : Route

    @Serializable
    data object WorthTheDrive : Route

    @Serializable
    data object MustSeePlaces : Route

    @Serializable
    data object Activities : Route

    @Serializable
    data object Essentials : Route

    /** Essentials opened on one module (from Search): expanded and scrolled into view. */
    @Serializable
    data class EssentialModule(val id: String) : Route

    @Serializable
    /** [category] is a UtilityCategory name, or [ALL_CATEGORIES]; [selectedId] pre-selects that pin. */
    data class UtilityMap(val category: String, val selectedId: String? = null) : Route

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
    /** [filter] is the list's [StoryKind] filter (null = All), so "Next story" follows the same list. */
    data class LocalFindDetail(val id: String, val filter: String? = null) : Route
}

/** [Route.UtilityMap] category value that shows every utility category on one map. */
const val ALL_CATEGORIES = "ALL"
