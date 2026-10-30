package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_category_activities
import kompass.composeapp.generated.resources.redesign_category_eat_and_drink
import kompass.composeapp.generated.resources.redesign_category_hidden_gems
import kompass.composeapp.generated.resources.redesign_category_practical
import kompass.composeapp.generated.resources.redesign_category_see_and_visit
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun PlaceCategory.labelRes(): StringResource = when (this) {
    PlaceCategory.EAT_AND_DRINK -> Res.string.redesign_category_eat_and_drink
    PlaceCategory.SEE_AND_VISIT -> Res.string.redesign_category_see_and_visit
    PlaceCategory.ACTIVITIES -> Res.string.redesign_category_activities
    PlaceCategory.HIDDEN_GEMS -> Res.string.redesign_category_hidden_gems
    PlaceCategory.PRACTICAL -> Res.string.redesign_category_practical
}

/** The place's own specific type (e.g. "Cathedral") when curated, else a localized category label. */
@Composable
fun Place.localizedTypeLabel(): String =
    subCategory?.trim()?.takeIf { it.isNotBlank() }?.toTitleCaseLabel()
        ?: stringResource(category.labelRes())
