package llc.bokadev.kompass.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val KompassTypography = Typography(
    // display — editorial hero
    displayLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 54.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.9).sp
    ),
    // headline — section and screen titles
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 35.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    // title — card titles
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    // body_large — descriptions and long text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    // body — lists, metadata, default copy
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // caption — subdued labels and metadata
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // overline — quiet mono-inspired labels
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.9.sp
    )
)
