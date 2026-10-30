package llc.bokadev.kompass.presentation.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Opens the app-wide language sheet (rendered once by `MainShell`) from any screen or ViewModel. */
class LanguageSheetHost {
    private val _isOpen = MutableStateFlow(false)
    val isOpen: StateFlow<Boolean> = _isOpen.asStateFlow()

    fun open() { _isOpen.value = true }
    fun close() { _isOpen.value = false }
}
