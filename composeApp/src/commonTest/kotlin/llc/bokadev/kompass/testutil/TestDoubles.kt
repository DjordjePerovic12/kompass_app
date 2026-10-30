package llc.bokadev.kompass.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen
import androidx.navigation.NavOptionsBuilder
import llc.bokadev.kompass.core.navigation.utils.NavType

/** Records calls to the template Navigator (core.navigation.utils) in order. */
class FakeNavigator : llc.bokadev.kompass.core.navigation.utils.Navigator {
    val recorded = mutableListOf<NavType>()
    override val navigationFlow: Flow<NavType> = emptyFlow()

    override suspend fun navigateTo(destination: Screen, navOptions: NavOptionsBuilder.() -> Unit) {
        recorded += NavType.NavigateToRoute(destination, navOptions)
    }

    override suspend fun navigateUp() {
        recorded += NavType.NavigateUp
    }

    override suspend fun switchTab(tab: BottomTab) {
        recorded += NavType.SwitchTab(tab)
    }

    /** Destinations from navigateTo, in order. */
    val destinations: List<Screen> get() = recorded.filterIsInstance<NavType.NavigateToRoute>().map { it.destination }
}

class NoOpAnalytics : AnalyticsRepository {
    override fun trackScreenView(screenName: String) = Unit
    override fun trackCategoryView(categoryKey: String, contentOrigin: String) = Unit
    override fun trackPlaceView(placeId: String, cityId: String?, zone: String?, placeCategory: String?, contentOrigin: String?) = Unit
    override fun trackActivityView(activityId: String, cityId: String?, zone: String?, contentOrigin: String?) = Unit
    override fun trackEventView(eventId: String, contentOrigin: String?) = Unit
    override fun trackInfoNoticeView(noticeId: String, cityId: String?, contentOrigin: String?) = Unit
    override fun trackNearbyView(zone: String?) = Unit
    override fun trackGuideOpen(activityId: String, cityId: String?) = Unit
    override fun trackAudioPlay(activityId: String, cityId: String?) = Unit
    override fun trackPremiumBundleOpen(contentOrigin: String?) = Unit
}
