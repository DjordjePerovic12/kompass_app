package llc.bokadev.kompass.core.util

import llc.bokadev.kompass.presentation.shared.UtilityMapPin

actual fun openUtilityCategoryInMaps(
    categoryQuery: String,
    centerLat: Double,
    centerLng: Double,
    currentLat: Double?,
    currentLng: Double?,
    fallbackLat: Double?,
    fallbackLng: Double?,
    pins: List<UtilityMapPin>
): Boolean = false
