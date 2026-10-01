package llc.bokadev.kompass.core.util

import llc.bokadev.kompass.presentation.shared.UtilityMapPin

expect fun openUtilityCategoryInMaps(
    categoryQuery: String,
    centerLat: Double,
    centerLng: Double,
    currentLat: Double? = null,
    currentLng: Double? = null,
    fallbackLat: Double? = null,
    fallbackLng: Double? = null,
    pins: List<UtilityMapPin> = emptyList()
): Boolean
