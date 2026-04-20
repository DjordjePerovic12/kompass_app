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

    override fun isFirstLaunch(): Boolean =
        prefs.getBoolean("is_first_launch", true)

    override fun setFirstLaunch(value: Boolean) {
        prefs.edit().putBoolean("is_first_launch", value).apply()
    }
}
