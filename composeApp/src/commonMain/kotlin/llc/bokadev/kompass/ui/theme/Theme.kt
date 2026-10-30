package llc.bokadev.kompass.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * App styling reads KompassTheme.colors / typography. The Material values below only exist so
 * Material components that read MaterialTheme defaults (snackbars, sheets, progress indicators,
 * default text) keep rendering exactly as before.
 */
private val materialColorScheme = lightColorScheme(
    primary = black,
    onPrimary = warmOffWhite,
    primaryContainer = black,
    onPrimaryContainer = black,
    secondary = black,
    onSecondary = warmOffWhite,
    secondaryContainer = black,
    onSecondaryContainer = black,
    tertiary = black,
    onTertiary = warmOffWhite,
    background = warmOffWhite,
    onBackground = black,
    surface = warmOffWhite,
    onSurface = black,
    surfaceVariant = warmStone,
    onSurfaceVariant = black,
    surfaceContainer = warmStone,
    surfaceContainerHigh = warmStone,
    outline = black,
    outlineVariant = black,
    error = black,
    onError = warmOffWhite
)

private val materialTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 54.sp, lineHeight = 56.sp, letterSpacing = (-0.9).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 35.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.9.sp)
)

private val materialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun KompassTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalKompassColors provides kompassColors,
        LocalKompassTypography provides kompassTypography
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = materialTypography,
            shapes = materialShapes,
            content = content
        )
    }
}

object KompassTheme {
    val colors: KompassColors
        @Composable get() = LocalKompassColors.current
    val typography: KompassTypography
        @Composable get() = LocalKompassTypography.current
}
