package llc.bokadev.kompass.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

/**
  Runtime app language switching, the multiplatform replacement for
  LocaleContextWrapper + attachBaseContext on Android.

  App() reads the selected language from AppPreferences.selectedLanguageFlow and wraps everything in
  AppEnvironment(languageTag). Changing the tag re-creates the UI in the new language, no restart needed.

 **/

expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun AppEnvironment(
    languageTag: String?,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppLocale provides languageTag
    ) {
        key(languageTag) {
            content()
        }
    }
}
