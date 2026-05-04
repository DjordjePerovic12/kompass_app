package llc.bokadev.kompass.core.util

import platform.Foundation.NSUserDefaults

class IosAppPreferences : AppPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getSelectedLanguage(): String =
        defaults.stringForKey("selected_language") ?: "en"

    override fun setSelectedLanguage(language: String) {
        defaults.setObject(language, forKey = "selected_language")
    }

    override fun isFirstLaunch(): Boolean {
        if (defaults.objectForKey("is_first_launch") == null) return true
        return defaults.boolForKey("is_first_launch")
    }

    override fun setFirstLaunch(value: Boolean) {
        defaults.setBool(value, forKey = "is_first_launch")
    }

    override fun hasAudioPass(): Boolean =
        defaults.boolForKey("has_audio_pass")

    override fun setAudioPass(value: Boolean) {
        defaults.setBool(value, forKey = "has_audio_pass")
    }

    override fun hasExplorerPass(): Boolean =
        defaults.boolForKey("has_explorer_pass")

    override fun setExplorerPass(value: Boolean) {
        defaults.setBool(value, forKey = "has_explorer_pass")
    }

    override fun hasPerksPass(): Boolean =
        defaults.boolForKey("has_perks_pass")

    override fun setPerksPass(value: Boolean) {
        defaults.setBool(value, forKey = "has_perks_pass")
    }
}
