package llc.bokadev.kompass.core.util

import platform.Foundation.NSUserDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosAppPreferences : AppPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    private val languageState = MutableStateFlow(
        defaults.stringForKey("selected_language") ?: "en"
    )

    override val selectedLanguageFlow: StateFlow<String> = languageState.asStateFlow()

    override fun getString(key: String): String? =
        defaults.stringForKey(key)

    override fun setString(key: String, value: String?) {
        if (value == null) {
            defaults.removeObjectForKey(key)
        } else {
            defaults.setObject(value, forKey = key)
        }
    }

    override fun getSelectedLanguage(): String =
        languageState.value

    override fun setSelectedLanguage(language: String) {
        defaults.setObject(language, forKey = "selected_language")
        languageState.value = language
    }

    override fun getAnonymousUserId(): String? =
        defaults.stringForKey("anonymous_user_id")

    override fun setAnonymousUserId(value: String) {
        defaults.setObject(value, forKey = "anonymous_user_id")
    }

    override fun isFirstLaunch(): Boolean {
        if (defaults.objectForKey("is_first_launch") == null) return true
        return defaults.boolForKey("is_first_launch")
    }

    override fun setFirstLaunch(value: Boolean) {
        defaults.setBool(value, forKey = "is_first_launch")
    }

    override fun hasSeenLocationEducationPrompt(): Boolean =
        defaults.boolForKey("has_seen_location_education_prompt")

    override fun setSeenLocationEducationPrompt(value: Boolean) {
        defaults.setBool(value, forKey = "has_seen_location_education_prompt")
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
