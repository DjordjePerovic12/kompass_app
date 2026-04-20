package llc.bokadev.kompass.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val KompassColorScheme = lightColorScheme(
    primary = colorNavy,
    onPrimary = colorWhite,
    primaryContainer = colorSlateGhost,
    onPrimaryContainer = colorNavy,
    secondary = colorAmber,
    onSecondary = colorWhite,
    secondaryContainer = colorAmberSubtle,
    onSecondaryContainer = colorNavy,
    tertiary = colorAmberDark,
    onTertiary = colorWhite,
    background = colorSurface,
    onBackground = colorNavy,
    surface = colorWhite,
    onSurface = colorNavy,
    surfaceVariant = colorSurfaceMid,
    onSurfaceVariant = colorNavyMuted,
    surfaceContainer = colorSurfaceMid,
    surfaceContainerHigh = colorSurfaceStrong,
    outline = colorSlateSoft,
    outlineVariant = colorSlatePale,
    error = colorError,
    onError = colorWhite
)

@Composable
fun KOmpassTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalKompassColors provides defaultKompassColors) {
        MaterialTheme(
            colorScheme = KompassColorScheme,
            typography = KompassTypography,
            shapes = KompassShapes,
            content = content
        )
    }
}
