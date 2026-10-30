package llc.bokadev.kompass.presentation.shared

import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.essentials_group_getting_around
import kompass.composeapp.generated.resources.essentials_group_local_tips
import kompass.composeapp.generated.resources.essentials_group_practical
import kompass.composeapp.generated.resources.essentials_group_registration
import kompass.composeapp.generated.resources.essentials_tile_atm
import kompass.composeapp.generated.resources.essentials_tile_emergency
import kompass.composeapp.generated.resources.essentials_tile_fuel
import kompass.composeapp.generated.resources.essentials_tile_parking
import kompass.composeapp.generated.resources.essentials_tile_pharmacy
import kompass.composeapp.generated.resources.essentials_tile_shop
import kompass.composeapp.generated.resources.essentials_tile_supermarket
import kompass.composeapp.generated.resources.essentials_title_atm
import kompass.composeapp.generated.resources.essentials_title_emergency
import kompass.composeapp.generated.resources.essentials_title_fuel
import kompass.composeapp.generated.resources.essentials_title_parking
import kompass.composeapp.generated.resources.essentials_title_pharmacy
import kompass.composeapp.generated.resources.essentials_title_shop
import kompass.composeapp.generated.resources.essentials_title_supermarket
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.UtilityCategory
import org.jetbrains.compose.resources.StringResource

/** The topic groups shown in Essential info, in display order. A UI mapping only; CMS categories are unchanged. */
enum class EssentialGroupKind(val category: EssentialCategory) {
    REGISTRATION(EssentialCategory.CUSTOMS),
    GETTING_AROUND(EssentialCategory.TRANSPORT),
    PRACTICAL(EssentialCategory.PRACTICAL),
    LOCAL_TIPS(EssentialCategory.TIPS)
}

fun EssentialGroupKind.titleRes(): StringResource = when (this) {
    EssentialGroupKind.REGISTRATION -> Res.string.essentials_group_registration
    EssentialGroupKind.GETTING_AROUND -> Res.string.essentials_group_getting_around
    EssentialGroupKind.PRACTICAL -> Res.string.essentials_group_practical
    EssentialGroupKind.LOCAL_TIPS -> Res.string.essentials_group_local_tips
}

fun UtilityCategory.tileLabelRes(): StringResource = when (this) {
    UtilityCategory.ATM -> Res.string.essentials_tile_atm
    UtilityCategory.PHARMACY -> Res.string.essentials_tile_pharmacy
    UtilityCategory.SUPERMARKET -> Res.string.essentials_tile_supermarket
    UtilityCategory.SHOP -> Res.string.essentials_tile_shop
    UtilityCategory.PARKING -> Res.string.essentials_tile_parking
    UtilityCategory.GAS_STATION -> Res.string.essentials_tile_fuel
    UtilityCategory.EMERGENCY -> Res.string.essentials_tile_emergency
}

fun UtilityCategory.cardTitleRes(): StringResource = when (this) {
    UtilityCategory.ATM -> Res.string.essentials_title_atm
    UtilityCategory.PHARMACY -> Res.string.essentials_title_pharmacy
    UtilityCategory.SUPERMARKET -> Res.string.essentials_title_supermarket
    UtilityCategory.SHOP -> Res.string.essentials_title_shop
    UtilityCategory.PARKING -> Res.string.essentials_title_parking
    UtilityCategory.GAS_STATION -> Res.string.essentials_title_fuel
    UtilityCategory.EMERGENCY -> Res.string.essentials_title_emergency
}
