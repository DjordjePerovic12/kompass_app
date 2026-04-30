package llc.bokadev.kompass.presentation.screens.experiences

import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel

data class ExperiencesState(
    override val isLoading: Boolean = false,
    override val error: String? = null
) : BaseState()

sealed interface ExperiencesEvent : BaseEvent

sealed interface ExperiencesSideEffect

class ExperiencesViewModel : BaseViewModel<ExperiencesState, ExperiencesEvent>() {

    override val initialState = ExperiencesState()

    override fun onIntent(event: ExperiencesEvent) {}
}
