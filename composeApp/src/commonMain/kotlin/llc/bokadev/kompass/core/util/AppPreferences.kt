package llc.bokadev.kompass.core.util

import kotlinx.coroutines.flow.StateFlow

interface AppPreferences {
    val selectedLanguageFlow: StateFlow<String>
    fun getString(key: String): String?
    fun setString(key: String, value: String?)
    fun getSelectedLanguage(): String
    fun setSelectedLanguage(language: String)
    fun getAnonymousUserId(): String?
    fun setAnonymousUserId(value: String)
    fun isFirstLaunch(): Boolean
    fun setFirstLaunch(value: Boolean)
    fun hasSeenLocationEducationPrompt(): Boolean
    fun setSeenLocationEducationPrompt(value: Boolean)
    fun hasAudioPass(): Boolean
    fun setAudioPass(value: Boolean)
    fun hasExplorerPass(): Boolean
    fun setExplorerPass(value: Boolean)
    fun hasPerksPass(): Boolean
    fun setPerksPass(value: Boolean)
}
