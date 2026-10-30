package llc.bokadev.kompass.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.geologica_bold
import kompass.composeapp.generated.resources.geologica_light
import kompass.composeapp.generated.resources.geologica_medium
import kompass.composeapp.generated.resources.geologica_regular
import kompass.composeapp.generated.resources.geologica_semibold
import kompass.composeapp.generated.resources.gloock_regular
import kompass.composeapp.generated.resources.helvetica_bold
import kompass.composeapp.generated.resources.helvetica_medium
import kompass.composeapp.generated.resources.helvetica_regular
import org.jetbrains.compose.resources.Font

/*
 * One FontFamily per typeface holding every weight file, so call sites that `.copy(fontWeight = …)`
 * a style keep getting the real font file instead of a synthesized weight. Geologica's static
 * weights were instanced from the variable font with fontTools. All sizes are `.sp` so they scale
 * with the system font size.
 */

val gloock: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.gloock_regular, FontWeight.Normal)
    )
val geologica: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.geologica_light, FontWeight.Light),
        Font(Res.font.geologica_regular, FontWeight.Normal),
        Font(Res.font.geologica_medium, FontWeight.Medium),
        Font(Res.font.geologica_semibold, FontWeight.SemiBold),
        Font(Res.font.geologica_bold, FontWeight.Bold)
    )
val helvetica: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.helvetica_regular, FontWeight.Normal),
        Font(Res.font.helvetica_medium, FontWeight.Medium),
        Font(Res.font.helvetica_bold, FontWeight.Bold)
    )

@Immutable
data class KompassTypography(
    val gloockRegular23: TextStyle,
    val gloockRegular26: TextStyle,
    val gloockRegular32: TextStyle,
    val gloockRegular34: TextStyle,
    val gloockRegular40: TextStyle,
    val geologicaRegular12: TextStyle,
    val geologicaRegular13: TextStyle,
    val geologicaRegular14: TextStyle,
    val geologicaMedium16: TextStyle,
    val geologicaSemiBold11: TextStyle,
    val geologicaSemiBold17: TextStyle,
    val geologicaBold12: TextStyle,
    val helveticaRegular14: TextStyle,
    val helveticaMedium13: TextStyle,
    val helveticaMedium16: TextStyle,
    val helveticaBold24: TextStyle,
    val helveticaBold44: TextStyle,
)

val LocalKompassTypography = staticCompositionLocalOf {
    KompassTypography(
        gloockRegular23 = TextStyle.Default,
        gloockRegular26 = TextStyle.Default,
        gloockRegular32 = TextStyle.Default,
        gloockRegular34 = TextStyle.Default,
        gloockRegular40 = TextStyle.Default,
        geologicaRegular12 = TextStyle.Default,
        geologicaRegular13 = TextStyle.Default,
        geologicaRegular14 = TextStyle.Default,
        geologicaMedium16 = TextStyle.Default,
        geologicaSemiBold11 = TextStyle.Default,
        geologicaSemiBold17 = TextStyle.Default,
        geologicaBold12 = TextStyle.Default,
        helveticaRegular14 = TextStyle.Default,
        helveticaMedium13 = TextStyle.Default,
        helveticaMedium16 = TextStyle.Default,
        helveticaBold24 = TextStyle.Default,
        helveticaBold44 = TextStyle.Default,
    )
}

val kompassTypography: KompassTypography
    @Composable get() {
        val gloock = gloock
        val geologica = geologica
        val helvetica = helvetica
        return KompassTypography(
            // Card titles
            gloockRegular23 = TextStyle(fontFamily = gloock, fontWeight = FontWeight.Normal, fontSize = 23.sp, lineHeight = 25.sp),
            // Section titles (notices, events)
            gloockRegular26 = TextStyle(fontFamily = gloock, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 30.sp),
            // "Must see" section title
            gloockRegular32 = TextStyle(fontFamily = gloock, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 36.sp),
            // Nearby front card title
            gloockRegular34 = TextStyle(fontFamily = gloock, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 35.sp),
            // Area name and screen titles
            gloockRegular40 = TextStyle(fontFamily = gloock, fontWeight = FontWeight.Normal, fontSize = 40.sp, lineHeight = 42.sp),
            // Bottom bar, unselected
            geologicaRegular12 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
            // Venue, secondary info
            geologicaRegular13 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
            // Descriptions
            geologicaRegular14 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
            // Button text, event title
            geologicaMedium16 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
            // UPPERCASE labels: apply `.uppercase()` to the string at the call site
            geologicaSemiBold11 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.12.em),
            // Notice titles, event time
            geologicaSemiBold17 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
            // Bottom bar, selected
            geologicaBold12 = TextStyle(fontFamily = geologica, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
            helveticaRegular14 = TextStyle(fontFamily = helvetica, fontWeight = FontWeight.Normal, fontSize = 14.sp),
            helveticaMedium13 = TextStyle(fontFamily = helvetica, fontWeight = FontWeight.Medium, fontSize = 13.sp),
            helveticaMedium16 = TextStyle(fontFamily = helvetica, fontWeight = FontWeight.Medium, fontSize = 16.sp),
            helveticaBold24 = TextStyle(fontFamily = helvetica, fontWeight = FontWeight.Bold, fontSize = 24.sp),
            helveticaBold44 = TextStyle(fontFamily = helvetica, fontWeight = FontWeight.Bold, fontSize = 44.sp),
        )
    }
