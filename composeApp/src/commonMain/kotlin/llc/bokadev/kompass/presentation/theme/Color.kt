package llc.bokadev.kompass.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand — deep navy
val colorNavy        = Color(0xFF102A43)
val colorNavyMedium  = Color(0xFF243B53)
val colorNavySubtle  = Color(0xFF334E68)
val colorNavyMuted   = Color(0xFF486581)
val colorSlate       = Color(0xFF627D98)
val colorSlateLight  = Color(0xFF829AB1)
val colorSlateSoft   = Color(0xFF9FB3C8)
val colorSlatePale   = Color(0xFFBCCCDC)
val colorSlateGhost  = Color(0xFFD9E2EC)
val colorSlateFaint  = Color(0xFFF0F4F8)

// Accent — warm amber
val colorAmberDark   = Color(0xFFD97706)
val colorAmber       = Color(0xFFF59E0B)
val colorAmberLight  = Color(0xFFFBBF24)
val colorAmberSubtle = Color(0xFFFEF3C7)

// Surfaces
val colorWhite         = Color(0xFFFFFFFF)
val colorSurface       = Color(0xFFF8F9FB)
val colorSurfaceMid    = Color(0xFFF1F3F7)
val colorSurfaceStrong = Color(0xFFE4E7ED)

// Semantic
val colorSuccess   = Color(0xFF059669)
val colorError     = Color(0xFFDC2626)
val colorSponsored = Color(0xFFF59E0B)

@Immutable
data class KompassColors(
    val colorNavy: Color,
    val colorNavyMedium: Color,
    val colorNavySubtle: Color,
    val colorNavyMuted: Color,
    val colorSlate: Color,
    val colorSlateLight: Color,
    val colorSlateSoft: Color,
    val colorSlatePale: Color,
    val colorSlateGhost: Color,
    val colorSlateFaint: Color,
    val colorAmberDark: Color,
    val colorAmber: Color,
    val colorAmberLight: Color,
    val colorAmberSubtle: Color,
    val colorWhite: Color,
    val colorSurface: Color,
    val colorSurfaceMid: Color,
    val colorSurfaceStrong: Color,
    val colorSuccess: Color,
    val colorError: Color,
    val colorSponsored: Color,
)

val LocalKompassColors = staticCompositionLocalOf {
    KompassColors(
        colorNavy = Color.Unspecified,
        colorNavyMedium = Color.Unspecified,
        colorNavySubtle = Color.Unspecified,
        colorNavyMuted = Color.Unspecified,
        colorSlate = Color.Unspecified,
        colorSlateLight = Color.Unspecified,
        colorSlateSoft = Color.Unspecified,
        colorSlatePale = Color.Unspecified,
        colorSlateGhost = Color.Unspecified,
        colorSlateFaint = Color.Unspecified,
        colorAmberDark = Color.Unspecified,
        colorAmber = Color.Unspecified,
        colorAmberLight = Color.Unspecified,
        colorAmberSubtle = Color.Unspecified,
        colorWhite = Color.Unspecified,
        colorSurface = Color.Unspecified,
        colorSurfaceMid = Color.Unspecified,
        colorSurfaceStrong = Color.Unspecified,
        colorSuccess = Color.Unspecified,
        colorError = Color.Unspecified,
        colorSponsored = Color.Unspecified,
    )
}

val defaultKompassColors = KompassColors(
    colorNavy = colorNavy,
    colorNavyMedium = colorNavyMedium,
    colorNavySubtle = colorNavySubtle,
    colorNavyMuted = colorNavyMuted,
    colorSlate = colorSlate,
    colorSlateLight = colorSlateLight,
    colorSlateSoft = colorSlateSoft,
    colorSlatePale = colorSlatePale,
    colorSlateGhost = colorSlateGhost,
    colorSlateFaint = colorSlateFaint,
    colorAmberDark = colorAmberDark,
    colorAmber = colorAmber,
    colorAmberLight = colorAmberLight,
    colorAmberSubtle = colorAmberSubtle,
    colorWhite = colorWhite,
    colorSurface = colorSurface,
    colorSurfaceMid = colorSurfaceMid,
    colorSurfaceStrong = colorSurfaceStrong,
    colorSuccess = colorSuccess,
    colorError = colorError,
    colorSponsored = colorSponsored,
)

object KompassTheme {
    val colors: KompassColors
        @Composable get() = LocalKompassColors.current
}
