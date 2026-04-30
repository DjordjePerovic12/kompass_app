package llc.bokadev.kompass.core.presentation.base

open class BaseState(
    open val isLoading: Boolean = false,
    open val error: String? = null
)
