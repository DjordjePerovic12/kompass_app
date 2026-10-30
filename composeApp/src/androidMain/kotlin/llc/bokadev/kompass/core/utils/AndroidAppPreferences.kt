package llc.bokadev.kompass.core.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidAppPreferences(private val context: Context) : AppPreferences {

    private val prefs by lazy {
        context.getSharedPreferences("kompass_prefs", Context.MODE_PRIVATE)
    }

    private val languageState = MutableStateFlow(
        prefs.getString("selected_language", "en") ?: "en"
    )

    override val selectedLanguageFlow: StateFlow<String> = languageState.asStateFlow()

    override fun getString(key: String): String? =
        prefs.getString(key, null)

    override fun setString(key: String, value: String?) {
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    override fun getSelectedLanguage(): String =
        languageState.value

    override fun setSelectedLanguage(language: String) {
        prefs.edit().putString("selected_language", language).apply()
        languageState.value = language
    }

    override fun getAnonymousUserId(): String? =
        prefs.getString("anonymous_user_id", null)

    override fun setAnonymousUserId(value: String) {
        prefs.edit().putString("anonymous_user_id", value).apply()
    }

    override fun isFirstLaunch(): Boolean =
        prefs.getBoolean("is_first_launch", true)

    override fun setFirstLaunch(value: Boolean) {
        prefs.edit().putBoolean("is_first_launch", value).apply()
    }

    override fun hasSeenLocationEducationPrompt(): Boolean =
        prefs.getBoolean("has_seen_location_education_prompt", false)

    override fun setSeenLocationEducationPrompt(value: Boolean) {
        prefs.edit().putBoolean("has_seen_location_education_prompt", value).apply()
    }
}
