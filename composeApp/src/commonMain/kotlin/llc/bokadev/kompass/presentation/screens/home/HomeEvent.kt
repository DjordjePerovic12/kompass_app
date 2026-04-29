package llc.bokadev.kompass.presentation.screens.home

sealed interface HomeEvent {
    data object LoadMustSeePlaces : HomeEvent
}