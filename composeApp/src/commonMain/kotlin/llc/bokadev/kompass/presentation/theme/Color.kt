package llc.bokadev.kompass.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =====================================================
// TEMPORARY MINIMAL PALETTE
// One real brand color + one fallback color while we rebuild the system.
// =====================================================

val colorMain = Color(0xFFDB7C26)
val colorBlack = Color(0xFF000000)
val colorWarmOffWhite = Color(0xFFF8F3EA)
val colorHeaderTitle = Color(0xFFF8F1E8)
val colorHeaderSubtitle = Color(0xFFF5E0C8)
val colorWarmStone = Color(0xFFE8DCC8)
val colorDustySage = Color(0xFFAEBBA2)
val colorSoftTerracotta = Color(0xFFC98B5A)
val colorMutedSky = Color(0xFF97A8B8)
val colorSoftLilac = Color(0xFFCDB7AE)
val colorSandGold = Color(0xFFD8C197)
val colorRoseClay = Color(0xFFB98979)

// Existing semantic/app aliases temporarily mapped to the minimal palette.
val colorNavy = colorBlack
val colorNavyMedium = colorBlack
val colorNavySubtle = colorBlack
val colorNavyMuted = colorBlack

val colorSlate = colorBlack
val colorSlateLight = colorBlack
val colorSlateSoft = colorBlack
val colorSlatePale = colorBlack
val colorSlateGhost = colorBlack
val colorSlateFaint = colorBlack

val colorAmberDark = colorBlack
val colorAmber = colorBlack
val colorAmberLight = colorBlack
val colorAmberSubtle = colorBlack

val colorSignal = colorBlack
val colorSignalStrong = colorBlack
val colorSignalSubtle = colorWarmStone

val colorAtmosphere = colorBlack

val colorEmberDark = colorBlack
val colorEmber = colorBlack

val colorWhite = colorWarmOffWhite

val colorSurface = colorWarmOffWhite
val colorSurfaceMid = colorWarmStone
val colorSurfaceStrong = colorWarmStone

val colorSuccess = colorBlack
val colorWarning = colorBlack
val colorError = colorBlack

val colorSponsored = colorBlack

val filterUnselectedSurface = colorWarmOffWhite
val filterUnselectedText = colorBlack

val dateBoxBackground = colorBlack
val eventDateText = colorBlack

val mustSeeChipBackground = colorBlack
val mustSeeChipContent = colorBlack

val colorDeepNavigation = colorBlack
val colorNavActive = colorBlack
val colorNavActiveContent = colorBlack
val colorNavInactive = colorBlack

val colorHomeCanvas = colorWarmOffWhite
val colorOrangeMain = colorMain
val colorHomeHeroSoft = colorWarmOffWhite
val colorHomeHeroMuted = colorBlack
val colorHomePanel = colorWarmOffWhite
val colorHomePanelBorder = colorWarmOffWhite

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

    val colorSignal: Color,
    val colorSignalStrong: Color,
    val colorSignalSubtle: Color,

    val colorAtmosphere: Color,

    val colorEmberDark: Color,
    val colorEmber: Color,

    val colorWhite: Color,

    val colorSurface: Color,
    val colorSurfaceMid: Color,
    val colorSurfaceStrong: Color,

    val colorSuccess: Color,
    val colorWarning: Color,
    val colorError: Color,

    val colorSponsored: Color,

    val filterUnselectedSurface: Color,
    val filterUnselectedText: Color,

    val dateBoxBackground: Color,
    val eventDateText: Color,

    val colorDeepNavigation: Color,
    val colorNavActive: Color,
    val colorNavActiveContent: Color,
    val colorNavInactive: Color,

    val colorHomeCanvas: Color,
    val colorOrangeMain: Color,
    val colorHomeHeroSoft: Color,
    val colorHomeHeroMuted: Color,
    val colorHomePanel: Color,
    val colorHomePanelBorder: Color,
)

val LocalKompassColors = staticCompositionLocalOf {
    KompassColors(
        colorNavy = colorBlack,
        colorNavyMedium = colorBlack,
        colorNavySubtle = colorBlack,
        colorNavyMuted = colorBlack,

        colorSlate = colorBlack,
        colorSlateLight = colorBlack,
        colorSlateSoft = colorBlack,
        colorSlatePale = colorBlack,
        colorSlateGhost = colorBlack,
        colorSlateFaint = colorBlack,

        colorAmberDark = colorBlack,
        colorAmber = colorBlack,
        colorAmberLight = colorBlack,
        colorAmberSubtle = colorBlack,

        colorSignal = colorBlack,
        colorSignalStrong = colorBlack,
        colorSignalSubtle = colorBlack,

        colorAtmosphere = colorBlack,

        colorEmberDark = colorBlack,
        colorEmber = colorBlack,

        colorWhite = colorBlack,

        colorSurface = colorBlack,
        colorSurfaceMid = colorBlack,
        colorSurfaceStrong = colorBlack,

        colorSuccess = colorBlack,
        colorWarning = colorBlack,
        colorError = colorBlack,

        colorSponsored = colorBlack,

        filterUnselectedSurface = colorBlack,
        filterUnselectedText = colorBlack,

        dateBoxBackground = colorBlack,
        eventDateText = colorBlack,

        colorDeepNavigation = colorBlack,
        colorNavActive = colorBlack,
        colorNavActiveContent = colorBlack,
        colorNavInactive = colorBlack,

        colorHomeCanvas = colorBlack,
        colorOrangeMain = colorMain,
        colorHomeHeroSoft = colorBlack,
        colorHomeHeroMuted = colorBlack,
        colorHomePanel = colorBlack,
        colorHomePanelBorder = colorBlack,
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

    colorSignal = colorSignal,
    colorSignalStrong = colorSignalStrong,
    colorSignalSubtle = colorSignalSubtle,

    colorAtmosphere = colorAtmosphere,

    colorEmberDark = colorEmberDark,
    colorEmber = colorEmber,

    colorWhite = colorWhite,

    colorSurface = colorSurface,
    colorSurfaceMid = colorSurfaceMid,
    colorSurfaceStrong = colorSurfaceStrong,

    colorSuccess = colorSuccess,
    colorWarning = colorWarning,
    colorError = colorError,

    colorSponsored = colorSponsored,

    filterUnselectedSurface = filterUnselectedSurface,
    filterUnselectedText = filterUnselectedText,

    dateBoxBackground = dateBoxBackground,
    eventDateText = eventDateText,

    colorDeepNavigation = colorDeepNavigation,
    colorNavActive = colorNavActive,
    colorNavActiveContent = colorNavActiveContent,
    colorNavInactive = colorNavInactive,

    colorHomeCanvas = colorHomeCanvas,
    colorOrangeMain = colorOrangeMain,
    colorHomeHeroSoft = colorHomeHeroSoft,
    colorHomeHeroMuted = colorHomeHeroMuted,
    colorHomePanel = colorHomePanel,
    colorHomePanelBorder = colorHomePanelBorder,
)

object KompassTheme {
    val colors: KompassColors
        @Composable get() = LocalKompassColors.current
}
