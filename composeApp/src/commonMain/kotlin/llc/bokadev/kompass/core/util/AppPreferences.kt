package llc.bokadev.kompass.core.util

interface AppPreferences {
    fun getSelectedLanguage(): String
    fun setSelectedLanguage(language: String)
    fun isFirstLaunch(): Boolean
    fun setFirstLaunch(value: Boolean)
}
