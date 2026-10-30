package llc.bokadev.kompass.core.utils

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalAppLocale = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (default == null) {
            default = Locale.getDefault()
        }

        val new = when (value) {
            null -> default!!
            else -> Locale.forLanguageTag(value)
        }
        // Compose resources resolve against androidx.compose.ui.text.intl.Locale.current,
        // which reads LocaleList.getDefault().
        Locale.setDefault(new)
        LocaleList.setDefault(LocaleList(new))
        val resources = LocalContext.current.resources
        val configuration = Configuration(resources.configuration).apply { setLocale(new) }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalAppLocale.provides(new.toLanguageTag())
    }
}
