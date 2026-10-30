package llc.bokadev.kompass.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue

/**
  Runtime app language switching, the multiplatform replacement for
  LocaleContextWrapper + attachBaseContext on Android.

  App() reads the selected language from AppPreferences.selectedLanguageFlow and wraps everything in
  AppEnvironment(languageTag). Each actual first switches the platform locale that Compose
  resources read (Locale.current), then provides the tag through a static CompositionLocal: a
  change recomposes the whole UI in the new language while keeping its state (selected tab,
  back stacks). The template's key(languageTag) would re-create the UI instead.
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
        LocalAppLocale provides languageTag,
        content = content
    )
}
