package llc.bokadev.kompass.presentation.screens.essentials

import llc.bokadev.kompass.core.util.EssentialsStrings
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.UtilityCategory

/** Known emergency services, so bundled and CMS numbers get translated labels. */
enum class EmergencyService { GENERAL, POLICE, FIRE, AMBULANCE }

data class EmergencyNumber(val number: String, val service: EmergencyService?, val rawLabel: String)

/** Montenegro's general emergency number (owner-approved, 2026-10-06); always the primary button. */
val GENERAL_EMERGENCY = EmergencyNumber("112", EmergencyService.GENERAL, "General emergency")

/**
 * Bundled copy of the CMS emergency entry, so the card renders offline on a fresh install.
 * Mirrors the CMS today (Police 122, Fire department 123, Ambulance 124).
 */
val BUNDLED_EMERGENCY_NUMBERS = listOf(
    EmergencyNumber("122", EmergencyService.POLICE, "Police"),
    EmergencyNumber("123", EmergencyService.FIRE, "Fire department"),
    EmergencyNumber("124", EmergencyService.AMBULANCE, "Ambulance")
)

private val numberLine = Regex("""^\s*(.+?)\s*[-–—:]\s*(\+?\d[\d ]{1,14})\s*$""")

/**
 * Parses "Police - 122" style lines from the CMS emergency entry. Lines that aren't
 * "label - number" are ignored; 112 is skipped because it is always shown as the primary button.
 */
fun parseEmergencyNumbers(content: String): List<EmergencyNumber> =
    content.lines().mapNotNull { line ->
        val match = numberLine.matchEntire(line) ?: return@mapNotNull null
        val label = match.groupValues[1].trim()
        val number = match.groupValues[2].replace(" ", "")
        EmergencyNumber(number, label.toEmergencyService(), label)
    }.filter { it.number != GENERAL_EMERGENCY.number }.distinctBy { it.number }

private fun String.toEmergencyService(): EmergencyService? = lowercase().let {
    when {
        "police" in it || "polic" in it -> EmergencyService.POLICE
        "fire" in it -> EmergencyService.FIRE
        "ambulance" in it || "medical" in it -> EmergencyService.AMBULANCE
        else -> null
    }
}

fun EmergencyNumber.label(strings: EssentialsStrings): String = when (service) {
    EmergencyService.GENERAL -> strings.generalEmergency
    EmergencyService.POLICE -> strings.police
    EmergencyService.FIRE -> strings.fire
    EmergencyService.AMBULANCE -> strings.ambulance
    null -> rawLabel
}

/** The topic groups shown in Essential info, in display order. A UI mapping only; CMS categories are unchanged. */
enum class EssentialGroupKind(val category: EssentialCategory) {
    REGISTRATION(EssentialCategory.CUSTOMS),
    GETTING_AROUND(EssentialCategory.TRANSPORT),
    PRACTICAL(EssentialCategory.PRACTICAL),
    LOCAL_TIPS(EssentialCategory.TIPS)
}

data class EssentialGroup(val kind: EssentialGroupKind, val modules: List<CityEssential>)

fun EssentialGroupKind.title(strings: EssentialsStrings): String = when (this) {
    EssentialGroupKind.REGISTRATION -> strings.groupRegistration
    EssentialGroupKind.GETTING_AROUND -> strings.groupGettingAround
    EssentialGroupKind.PRACTICAL -> strings.groupPractical
    EssentialGroupKind.LOCAL_TIPS -> strings.groupLocalTips
}

fun EssentialGroupKind.purpose(strings: EssentialsStrings): String = when (this) {
    EssentialGroupKind.REGISTRATION -> strings.purposeRegistration
    EssentialGroupKind.GETTING_AROUND -> strings.purposeGettingAround
    EssentialGroupKind.PRACTICAL -> strings.purposePractical
    EssentialGroupKind.LOCAL_TIPS -> strings.purposeLocalTips
}

/** A module worth rendering: it has an answer or at least one action (never a header-only row). */
fun CityEssential.hasSubstance(lang: String): Boolean =
    localizedContent(lang).isNotBlank() ||
        links.any { it.isNotBlank() } ||
        resolvedLocations(lang).any { it.latitude != null && it.longitude != null }

/** First paragraph of the CMS text, for the priority card summary (verified copy only, never rewritten). */
fun String.firstParagraph(): String =
    trim().split(Regex("\\n\\s*\\n")).firstOrNull()?.trim().orEmpty()

/** A readable title for a bare CMS link: its host without "www.", e.g. "redtaxikotor.com". */
fun String.linkTitle(): String =
    substringAfter("://").substringBefore("/").removePrefix("www.").ifBlank { this }

/** "86 m", "130 m" (10 m steps above 100 m), "1.2 km". */
fun Int.formatDistance(): String = when {
    this < 100 -> "$this m"
    this < 1000 -> "${(this + 5) / 10 * 10} m"
    else -> {
        val tenths = (this + 50) / 100
        "${tenths / 10}.${tenths % 10} km"
    }
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

/** Categories that get a full summary card (map + nearest list), in order; the rest get compact rows. */
val SUMMARY_UTILITY_CATEGORIES = listOf(UtilityCategory.ATM, UtilityCategory.PHARMACY)
