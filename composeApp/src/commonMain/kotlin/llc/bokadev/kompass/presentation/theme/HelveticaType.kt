package llc.bokadev.kompass.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.helvetica_bold
import kompass.composeapp.generated.resources.helvetica_medium
import kompass.composeapp.generated.resources.helvetica_regular
import org.jetbrains.compose.resources.Font

@Composable
private fun helveticaFamily() = FontFamily(
    Font(Res.font.helvetica_regular, FontWeight.Normal),
    Font(Res.font.helvetica_medium, FontWeight.Medium),
    Font(Res.font.helvetica_bold, FontWeight.Bold),
)

@Composable
private fun helveticaStyle(weight: FontWeight, size: Int) = TextStyle(
    fontFamily = helveticaFamily(),
    fontWeight = weight,
    fontSize = size.sp,
)

// Regular (400)
@Composable fun helveticaRegular11() = helveticaStyle(FontWeight.Normal, 11)
@Composable fun helveticaRegular13() = helveticaStyle(FontWeight.Normal, 13)
@Composable fun helveticaRegular14() = helveticaStyle(FontWeight.Normal, 14)
@Composable fun helveticaRegular16() = helveticaStyle(FontWeight.Normal, 16)
@Composable fun helveticaRegular18() = helveticaStyle(FontWeight.Normal, 18)
@Composable fun helveticaRegular20() = helveticaStyle(FontWeight.Normal, 20)
@Composable fun helveticaRegular24() = helveticaStyle(FontWeight.Normal, 24)

// Medium (500)
@Composable fun helveticaMedium11() = helveticaStyle(FontWeight.Medium, 11)
@Composable fun helveticaMedium13() = helveticaStyle(FontWeight.Medium, 13)
@Composable fun helveticaMedium14() = helveticaStyle(FontWeight.Medium, 14)
@Composable fun helveticaMedium16() = helveticaStyle(FontWeight.Medium, 16)
@Composable fun helveticaMedium18() = helveticaStyle(FontWeight.Medium, 18)
@Composable fun helveticaMedium20() = helveticaStyle(FontWeight.Medium, 20)
@Composable fun helveticaMedium24() = helveticaStyle(FontWeight.Medium, 24)

// Bold (700)
@Composable fun helveticaBold13() = helveticaStyle(FontWeight.Bold, 13)
@Composable fun helveticaBold14() = helveticaStyle(FontWeight.Bold, 14)
@Composable fun helveticaBold16() = helveticaStyle(FontWeight.Bold, 16)
@Composable fun helveticaBold20() = helveticaStyle(FontWeight.Bold, 20)
@Composable fun helveticaBold24() = helveticaStyle(FontWeight.Bold, 24)
@Composable fun helveticaBold28() = helveticaStyle(FontWeight.Bold, 28)
@Composable fun helveticaBold35() = helveticaStyle(FontWeight.Bold, 35)
@Composable fun helveticaBold44() = helveticaStyle(FontWeight.Bold, 44)
@Composable fun helveticaBold48() = helveticaStyle(FontWeight.Bold, 48)
@Composable fun helveticaBold54() = helveticaStyle(FontWeight.Bold, 54)
