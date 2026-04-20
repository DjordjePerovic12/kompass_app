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
}
