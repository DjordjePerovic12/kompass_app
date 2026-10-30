package llc.bokadev.kompass.core.utils

fun String.toTitleCaseLabel(): String =
    replace('_', ' ')
        .replace('-', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }

/**
 * Uppercase that respects Turkish dotted/dotless i (i → İ, ı → I). Common Kotlin's `uppercase()`
 * is locale-invariant, so labels like "PERAST" would otherwise render "I" for Turkish "i".
 */
fun String.uppercaseFor(lang: String): String =
    if (lang == "tr") map { if (it == 'i') 'İ' else it }.joinToString("").uppercase() else uppercase()
