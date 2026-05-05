package llc.bokadev.kompass.core.util

interface AppPreferences {
    fun getSelectedLanguage(): String
    fun setSelectedLanguage(language: String)
    fun getAnonymousUserId(): String?
    fun setAnonymousUserId(value: String)
    fun isFirstLaunch(): Boolean
    fun setFirstLaunch(value: Boolean)
    fun hasAudioPass(): Boolean
    fun setAudioPass(value: Boolean)
    fun hasExplorerPass(): Boolean
    fun setExplorerPass(value: Boolean)
    fun hasPerksPass(): Boolean
    fun setPerksPass(value: Boolean)
}
