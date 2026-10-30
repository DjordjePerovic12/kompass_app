package llc.bokadev.kompass.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * "Lamplight" design tokens for the Home screen (and the globally-shared floating nav bar).
 * A typed, swappable token set rather than a plain object — see the spec's §1.1 "alternative
 * dark partners" note — so the whole dark background can be re-derived in one place later
 * without touching call sites. Scoped to Home/nav-bar only: Explore/Saved/Essentials keep
 * reading [KompassTheme.colors] as before: this is just an additional value made available via
 * [CompositionLocalProvider], not a replacement of the app-wide theme.
 */
@Immutable
data class LamplightColors(
    val lamp: Color,
    val night: Color,
    val surfaceRaised: Color,
    /** [surfaceRaised] warmed by `lamp`: the row whose audio is loaded (audio library). */
    val surfaceActive: Color,
    val navBar: Color,
    val surfaceMuted: Color,
    val divider: Color,
    val borderStrong: Color,
    val indicatorInactive: Color,
    val limestone: Color,
    /** Limestone −6%: image placeholder/fallback block on limestone cards. */
    val limestoneShade: Color,
    /** Outline glyph drawn on [limestoneShade] (e.g. the missing-photo landmark). */
    val landmarkGlyph: Color,
    val chalk: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnLimestone: Color,
    val alert: Color
)

/** The approved partner for `lamp`. Alternatives in spec §1.1 are [NEEDS APPROVAL] — not wired up. */
val DefaultLamplightColors = LamplightColors(
    lamp = Color(0xFFFFC247),
    night = Color(0xFF12161A),
    surfaceRaised = Color(0xFF1E2327),
    surfaceActive = Color(0xFF2A2516),
    navBar = Color(0xFF262B2F),
    surfaceMuted = Color(0xFF2A3034),
    divider = Color(0xFF2C3236),
    borderStrong = Color(0xFF3A4146),
    indicatorInactive = Color(0xFF4A5055),
    limestone = Color(0xFFD9DAD3),
    limestoneShade = Color(0xFFC9CAC3),
    landmarkGlyph = Color(0xFF7A7F78),
    chalk = Color(0xFFF4F4EF),
    textPrimary = Color(0xFFE6E7E0),
    textSecondary = Color(0xFFB7BBB3),
    textTertiary = Color(0xFF9EA39C),
    textOnLimestone = Color(0xFF3C4246),
    alert = Color(0xFFFF6B57)
)

val LocalLamplightColors = staticCompositionLocalOf { DefaultLamplightColors }

object LamplightTheme {
    val colors: LamplightColors
        @Composable get() = LocalLamplightColors.current
}

@Composable
fun ProvideLamplightTheme(
    colors: LamplightColors = DefaultLamplightColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLamplightColors provides colors, content = content)
}
