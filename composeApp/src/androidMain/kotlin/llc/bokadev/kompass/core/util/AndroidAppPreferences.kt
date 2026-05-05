package llc.bokadev.kompass.core.util

import android.content.Context

class AndroidAppPreferences(private val context: Context) : AppPreferences {

    private val prefs by lazy {
        context.getSharedPreferences("kompass_prefs", Context.MODE_PRIVATE)
    }

    override fun getSelectedLanguage(): String =
        prefs.getString("selected_language", "en") ?: "en"

    override fun setSelectedLanguage(language: String) {
        prefs.edit().putString("selected_language", language).apply()
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

    override fun hasAudioPass(): Boolean =
        prefs.getBoolean("has_audio_pass", false)

    override fun setAudioPass(value: Boolean) {
        prefs.edit().putBoolean("has_audio_pass", value).apply()
    }

    override fun hasExplorerPass(): Boolean =
        prefs.getBoolean("has_explorer_pass", false)

    override fun setExplorerPass(value: Boolean) {
        prefs.edit().putBoolean("has_explorer_pass", value).apply()
    }

    override fun hasPerksPass(): Boolean =
        prefs.getBoolean("has_perks_pass", false)

    override fun setPerksPass(value: Boolean) {
        prefs.edit().putBoolean("has_perks_pass", value).apply()
    }
}
