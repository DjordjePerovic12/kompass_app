package llc.bokadev.kompass.core.utils

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun deviceLanguageCode(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.substringBefore('-') ?: "en"
