package llc.bokadev.kompass.presentation.essentials

import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState
import llc.bokadev.kompass.presentation.essentials.utils.BUNDLED_EMERGENCY_NUMBERS
import llc.bokadev.kompass.presentation.essentials.utils.EmergencyNumber
import llc.bokadev.kompass.presentation.essentials.utils.EssentialGroup
import llc.bokadev.kompass.presentation.essentials.utils.UtilityCategorySection

data class EssentialsState(
    override val isLoading: Boolean = true,
    /** Essentials has independent parts (info, emergency, utilities); each shows its own failure. */
    override val error: BaseError? = null,
    /** Opened from Search on one module: expand it, scroll to it, and show a Back. */
    val focusModuleId: String? = null,
    val selectedTab: EssentialsTab = EssentialsTab.INFO,
    /** "Do this early": the first registration/customs entry; null hides the card. */
    val priority: CityEssential? = null,
    /**
     * The visitor hid the arrival reminder on this device. Only the reminder is hidden; it never
     * means "registered", and the full registration module stays in its group.
     */
    val reminderHidden: Boolean = false,
    /** Secondary emergency tiles (112 is always the primary button). Bundled until the CMS loads. */
    val emergencyNumbers: List<EmergencyNumber> = BUNDLED_EMERGENCY_NUMBERS,
    val groups: List<EssentialGroup> = emptyList(),
    val expandedIds: Set<String> = emptySet(),
    val essentialsFailed: Boolean = false,
    val sections: List<UtilityCategorySection> = emptyList(),
    val utilitiesFailed: Boolean = false,
    /** Null when location is off or unavailable: distances are then hidden, never "closest to you". */
    val currentLocation: GeoPoint? = null
) : BaseViewState(isLoading, error)

enum class EssentialsTab {
    INFO,
    UTILITY
}
