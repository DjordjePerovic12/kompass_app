package llc.bokadev.kompass.presentation.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventsScreen(
    vmKey: String = "events",
    onBack: () -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {}
) {
    val vm: EventsViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()
    val strings = rememberAppStrings()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("events")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = strings.eventsSlug,
                title = strings.eventsTitle,
                subtitle = strings.eventsSubtitle,
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        EventsScreenContent(
            state = state,
            onDateFilterSelected = {
                vm.onIntent(EventsEvent.SelectDateFilter(it))
            },
            onTypeFilterSelected = {
                vm.onIntent(EventsEvent.SelectTypeFilter(it))
            },
            onEventClick = { id ->
                analytics.trackEventView(id, contentOrigin = "events_screen")
                onNavigateToEventDetail(id)
            },
            onBack = onBack,
        )
    }
}
