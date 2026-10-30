package llc.bokadev.kompass.core.utils

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
}
