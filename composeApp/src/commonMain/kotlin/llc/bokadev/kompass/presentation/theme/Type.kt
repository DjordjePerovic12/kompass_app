package llc.bokadev.kompass.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val KompassTypography = Typography(
    // display — 32sp Bold — hero text, screen titles
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = (32 * 1.2).sp
    ),
    // headline — 24sp Bold — section headers
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = (24 * 1.2).sp
    ),
    // title — 20sp SemiBold — card titles, modal headers
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = (20 * 1.3).sp
    ),
    // body_large — 16sp Regular — descriptions, long text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.5).sp
    ),
    // body — 14sp Regular — default body, list items
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = (14 * 1.5).sp
    ),
    // caption — 12sp Medium — labels, badges, metadata
    bodySmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = (12 * 1.4).sp
    ),
    // overline — 10sp SemiBold uppercase — category labels, tags
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = (10 * 1.4).sp,
        letterSpacing = 0.8.sp
    )
)
