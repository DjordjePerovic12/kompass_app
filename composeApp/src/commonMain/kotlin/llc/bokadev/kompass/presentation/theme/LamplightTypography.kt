package llc.bokadev.kompass.presentation.theme

import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.Font

/**
 * "Lamplight" type styles (spec §1.2). Gloock (400 only) for display/headings, Geologica
 * (300/400/500/600/700, instanced from the variable font with fontTools since Compose
 * Multiplatform resource fonts need static weight files) for everything else. Both bundled as
 * composeResources — no downloadable fonts. All sizes are `.sp` so they scale with the system
 * font size per the spec's accessibility requirement.
 */
@Composable
private fun gloockFamily() = FontFamily(
    Font(Res.font.gloock_regular, FontWeight.Normal)
)

@Composable
private fun geologicaFamily() = FontFamily(
    Font(Res.font.geologica_light, FontWeight.Light),
    Font(Res.font.geologica_regular, FontWeight.Normal),
    Font(Res.font.geologica_medium, FontWeight.Medium),
    Font(Res.font.geologica_semibold, FontWeight.SemiBold),
    Font(Res.font.geologica_bold, FontWeight.Bold)
)

@Composable
private fun gloock(size: Int, lineHeight: Int) = TextStyle(
    fontFamily = gloockFamily(),
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

@Composable
private fun geologica(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = geologicaFamily(),
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

object LamplightType {
    /** Area name in the top bar. */
    @Composable fun areaTitle() = gloock(40, 42)

    /** Nearby front card title. */
    @Composable fun cardTitleXL() = gloock(34, 35)

    /** "Must see". */
    @Composable fun sectionTitle() = gloock(32, 36)

    /** "Notices", "Events". */
    @Composable fun sectionTitleS() = gloock(26, 30)

    /** Must See card title. */
    @Composable fun cardTitle() = gloock(23, 25)

    /** Notice titles, event time. */
    @Composable fun bodyL() = geologica(17, 24, FontWeight.SemiBold)

    /** Button text, event title. Spec allows 500-600; 500 is the default, callers may `.copy()`. */
    @Composable fun body() = geologica(16, 22, FontWeight.Medium)

    /** Descriptions. Spec allows 350-400; 400 is used (no 350 static instance). */
    @Composable fun bodyS() = geologica(14, 20, FontWeight.Normal)

    /** Venue, secondary info. */
    @Composable fun meta() = geologica(13, 18, FontWeight.Normal)

    /** UPPERCASE labels — apply `.uppercase()` to the string at the call site. */
    @Composable fun label() = geologica(11, 14, FontWeight.SemiBold).copy(letterSpacing = 0.12.em)

    /** Bottom bar. 400 unselected; pass `selected = true` for the 700-weight selected state. */
    @Composable fun navLabel(selected: Boolean = false) =
        geologica(12, 16, if (selected) FontWeight.Bold else FontWeight.Normal)
}
