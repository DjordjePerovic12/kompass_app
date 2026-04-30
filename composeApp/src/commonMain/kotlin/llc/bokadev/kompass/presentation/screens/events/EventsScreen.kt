package llc.bokadev.kompass.presentation.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventsScreen(
    vmKey: String = "events",
    onNavigateToEventDetail: (String) -> Unit = {}
) {
    val vm: EventsViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Whats on in Kotor",
                title = "Events",
                onBackClick = {

                }
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
            onEventClick = {},
            onBack = {},
        )
    }
}
