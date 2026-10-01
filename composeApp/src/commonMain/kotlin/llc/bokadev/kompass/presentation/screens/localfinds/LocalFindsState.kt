package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.LocalFind

data class LocalFindsState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val items: List<LocalFind> = emptyList()
) : BaseState()
