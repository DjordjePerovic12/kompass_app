package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.distanceKmTo

/**
 * Interactive utility map. Selection is by [UtilityMapPin.id] only (names can repeat).
 * The camera frames the pins when the pin set changes and moves to [focusedPinId] when the
 * selection changes; a [currentLocation] update alone never moves the camera.
 * [currentLocationLabel] is the localized title of the "you" marker.
 */
@Composable
expect fun NativeUtilityDetailMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    currentLocationLabel: String,
    focusedPinId: String?,
    onPinSelected: (id: String) -> Unit,
    modifier: Modifier = Modifier,
    /** Incrementing this reframes all pins (the visible Recenter action). */
    recenterRequest: Int = 0,
    /** Height covered by overlays at the top/bottom (e.g. the selection sheet); framing keeps pins clear of it. */
    obscuredTop: Dp = 0.dp,
    obscuredBottom: Dp = 0.dp
)

/** What the utility map camera should do after an update. */
sealed interface UtilityMapCameraMove {
    /** Fit all [points] (the pins, plus the user when close enough to be useful). */
    data class Frame(val points: List<GeoPoint>) : UtilityMapCameraMove
    /** Pan to the newly selected pin, keeping the zoom. */
    data class FocusOn(val point: GeoPoint) : UtilityMapCameraMove
    data object None : UtilityMapCameraMove
}

/**
 * Remembers what the camera last did, so only a new pin set or a new selection moves it.
 * Location refreshes and redraws leave the visitor's pan/zoom alone.
 */
class UtilityMapCameraTracker {
    private var framedIds: List<String>? = null
    private var focusedId: String? = null
    private var lastRecenter = 0

    fun next(pins: List<UtilityMapPin>, focusedPinId: String?, currentLocation: GeoPoint?, recenterRequest: Int = 0): UtilityMapCameraMove {
        val ids = pins.map { it.id }
        val focusChanged = focusedPinId != focusedId
        focusedId = focusedPinId
        val recenter = recenterRequest != lastRecenter
        lastRecenter = recenterRequest
        if (ids != framedIds || recenter) {
            framedIds = ids
            val points = pins.map { it.point }
            val user = currentLocation?.takeIf { loc -> points.any { it.distanceKmTo(loc) <= USER_IN_FRAME_KM } }
            return UtilityMapCameraMove.Frame(points + listOfNotNull(user))
        }
        if (focusChanged) {
            pins.firstOrNull { it.id == focusedPinId }?.let { return UtilityMapCameraMove.FocusOn(it.point) }
        }
        return UtilityMapCameraMove.None
    }

    private companion object {
        /** Further than this from every pin, including the user would zoom the map out too far. */
        const val USER_IN_FRAME_KM = 3.0
    }
}
