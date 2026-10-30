package llc.bokadev.kompass.core.util

import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory

fun PlaceCategory.localizedLabel(strings: AppStrings): String = when (this) {
    PlaceCategory.EAT_AND_DRINK -> strings.redesign.categoryEatAndDrink
    PlaceCategory.SEE_AND_VISIT -> strings.redesign.categorySeeAndVisit
    PlaceCategory.ACTIVITIES -> strings.redesign.categoryActivities
    PlaceCategory.HIDDEN_GEMS -> strings.redesign.categoryHiddenGems
    PlaceCategory.PRACTICAL -> strings.redesign.categoryPractical
}

/** The place's own specific type (e.g. "Cathedral") when curated, else a localized category label. */
fun Place.localizedTypeLabel(strings: AppStrings): String =
    subCategory?.trim()?.takeIf { it.isNotBlank() }?.toTitleCaseLabel()
        ?: category.localizedLabel(strings)

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
