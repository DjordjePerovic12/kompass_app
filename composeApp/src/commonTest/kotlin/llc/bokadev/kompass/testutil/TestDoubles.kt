package llc.bokadev.kompass.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import llc.bokadev.kompass.core.presentation.mvi.NavigationCommand
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.navigation.BottomTab
import llc.bokadev.kompass.presentation.navigation.Route

/** Records what a ViewModel asked the Navigator to do, synchronously. */
class RecordingNavigator : Navigator {
    val recorded = mutableListOf<NavigationCommand>()
    override val commands: Flow<NavigationCommand> = emptyFlow()

    override fun navigateTo(route: Route, popUpToRoute: Route?, popUpToInclusive: Boolean, isSingleTop: Boolean, restoreState: Boolean) {
        recorded += NavigationCommand.NavigateTo(route, popUpToRoute, popUpToInclusive, isSingleTop, restoreState)
    }

    override fun navigateUp() {
        recorded += NavigationCommand.NavigateUp
    }

    override fun switchTab(tab: BottomTab) {
        recorded += NavigationCommand.SwitchTab(tab)
    }

    /** Routes from navigateTo, in order. */
    val routes: List<Route> get() = recorded.filterIsInstance<NavigationCommand.NavigateTo>().map { it.route }
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
