package llc.bokadev.kompass.core.navigation

import kotlinx.serialization.Serializable
import llc.bokadev.kompass.core.components.BottomTab

/**
 * Every navigation destination in the app. Destinations without arguments are data objects,
 * destinations with arguments are data classes. Route arguments must be primitives or Strings
 * (an enum crashes on iOS at start-up), so enums travel as their `name`.
 */
@Serializable
sealed interface Screen {

    @Serializable
    data object LanguagePickerScreen : Screen

    /** Hosts the tab shell (MainShell) with one NavController per tab. */
    @Serializable
    data object MainScreen : Screen

    /** One tab's nested graph; [tab] is a [BottomTab] name. */
    @Serializable
    data class TabGraph(val tab: String = BottomTab.Home.name) : Screen

    @Serializable
    data object HomeScreen : Screen

    @Serializable
    data object ExploreScreen : Screen

    /** [autoplay] starts the place's audio once it has loaded (Home "Listen"). */
    @Serializable
    data class PlaceDetailScreen(val id: String, val autoplay: Boolean = false) : Screen

    @Serializable
    data class EventDetailScreen(val id: String) : Screen

    @Serializable
    data object EventsScreen : Screen

    @Serializable
    data class ExperienceDetailScreen(val id: String) : Screen

    /**
     * The shared Lamplight list. [kind] is a CategoryListKind name; [area] (a HomeArea preference
     * value such as "perast") pre-applies the area filter, e.g. from Home's manual-area "See all".
     */
    @Serializable
    data class CategoryListScreen(val kind: String, val area: String? = null) : Screen

    @Serializable
    data object WorthTheDriveScreen : Screen

    @Serializable
    data object EssentialsScreen : Screen

    /** Essentials opened on one module (from Search): expanded and scrolled into view. */
    @Serializable
    data class EssentialModuleScreen(val id: String) : Screen

    /** [category] is a UtilityCategory name, or [ALL_CATEGORIES]; [selectedId] pre-selects that pin. */
    @Serializable
    data class UtilityMapScreen(val category: String, val selectedId: String? = null) : Screen

    @Serializable
    data object ServicesScreen : Screen

    @Serializable
    data class ServiceDetailScreen(val id: String) : Screen

    /** The audio library. */
    @Serializable
    data object ListenScreen : Screen

    @Serializable
    data object FavoritesScreen : Screen

    @Serializable
    data object SearchScreen : Screen

    @Serializable
    data object InfoCenterScreen : Screen

    @Serializable
    data class InfoCenterDetailScreen(val id: String) : Screen

    @Serializable
    data object LocalFindsScreen : Screen

    /** [filter] is the list's StoryKind filter (null = All), so "Next story" follows the same list. */
    @Serializable
    data class LocalFindDetailScreen(val id: String, val filter: String? = null) : Screen
}

/** [Screen.UtilityMapScreen] category value that shows every utility category on one map. */
const val ALL_CATEGORIES = "ALL"
