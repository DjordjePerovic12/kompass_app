package llc.bokadev.kompass.core.utils

import java.util.Locale

actual fun deviceLanguageCode(): String = Locale.getDefault().language
