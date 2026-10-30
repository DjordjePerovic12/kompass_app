package llc.bokadev.kompass.presentation.base

import org.jetbrains.compose.resources.StringResource

sealed class BaseError(
    open val messageRes: StringResource
) : Throwable() {

    data class AlertError(
        val title: StringResource? = null,
        override val messageRes: StringResource
    ) : BaseError(messageRes)

    data class InlineError(
        override val messageRes: StringResource
    ) : BaseError(messageRes)

    data class IgnoreError(override val messageRes: StringResource) : BaseError(messageRes)
}
