package llc.bokadev.kompass.presentation.languagepicker

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class LanguagePickerEvent : BaseEvent {
    data class OnLanguageSelect(val code: String) : LanguagePickerEvent()
    data object OnUsePhoneLanguageClick : LanguagePickerEvent()
}
