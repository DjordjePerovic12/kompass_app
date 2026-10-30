package llc.bokadev.kompass.core.util

import java.util.Locale

actual fun deviceLanguageCode(): String = Locale.getDefault().language
