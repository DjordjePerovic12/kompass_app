package llc.bokadev.kompass.presentation.shared

import llc.bokadev.kompass.core.utils.EssentialsStrings
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.UtilityCategory

/** The topic groups shown in Essential info, in display order. A UI mapping only; CMS categories are unchanged. */
enum class EssentialGroupKind(val category: EssentialCategory) {
    REGISTRATION(EssentialCategory.CUSTOMS),
    GETTING_AROUND(EssentialCategory.TRANSPORT),
    PRACTICAL(EssentialCategory.PRACTICAL),
    LOCAL_TIPS(EssentialCategory.TIPS)
}

fun EssentialGroupKind.title(strings: EssentialsStrings): String = when (this) {
    EssentialGroupKind.REGISTRATION -> strings.groupRegistration
    EssentialGroupKind.GETTING_AROUND -> strings.groupGettingAround
    EssentialGroupKind.PRACTICAL -> strings.groupPractical
    EssentialGroupKind.LOCAL_TIPS -> strings.groupLocalTips
}

fun UtilityCategory.tileLabel(strings: EssentialsStrings): String = when (this) {
    UtilityCategory.ATM -> strings.tileAtm
    UtilityCategory.PHARMACY -> strings.tilePharmacy
    UtilityCategory.SUPERMARKET -> strings.tileSupermarket
    UtilityCategory.SHOP -> strings.tileShop
    UtilityCategory.PARKING -> strings.tileParking
    UtilityCategory.GAS_STATION -> strings.tileFuel
    UtilityCategory.EMERGENCY -> strings.tileEmergency
}

fun UtilityCategory.cardTitle(strings: EssentialsStrings): String = when (this) {
    UtilityCategory.ATM -> strings.titleAtm
    UtilityCategory.PHARMACY -> strings.titlePharmacy
    UtilityCategory.SUPERMARKET -> strings.titleSupermarket
    UtilityCategory.SHOP -> strings.titleShop
    UtilityCategory.PARKING -> strings.titleParking
    UtilityCategory.GAS_STATION -> strings.titleFuel
    UtilityCategory.EMERGENCY -> strings.titleEmergency
}
