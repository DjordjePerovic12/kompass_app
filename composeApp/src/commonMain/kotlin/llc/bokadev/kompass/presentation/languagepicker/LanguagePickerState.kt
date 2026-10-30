package llc.bokadev.kompass.presentation.languagepicker

import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** Nothing loads: the picker shows the bundled language list straight away. */
data class LanguagePickerState(
    override val isLoading: Boolean = false,
    override val error: BaseError? = null
) : BaseViewState(isLoading, error)
