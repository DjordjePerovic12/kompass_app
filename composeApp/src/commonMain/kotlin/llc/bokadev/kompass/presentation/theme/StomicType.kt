package llc.bokadev.kompass.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.stomic_regular
import org.jetbrains.compose.resources.Font

@Composable
private fun stomicFamily() = FontFamily(
    Font(Res.font.stomic_regular, FontWeight.Normal),
)

@Composable
private fun stomicStyle(size: Int) = TextStyle(
    fontFamily = stomicFamily(),
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
)

@Composable fun stomic11() = stomicStyle(11)
@Composable fun stomic13() = stomicStyle(13)
@Composable fun stomic14() = stomicStyle(14)
@Composable fun stomic16() = stomicStyle(16)
@Composable fun stomic18() = stomicStyle(18)
@Composable fun stomic20() = stomicStyle(20)
@Composable fun stomic24() = stomicStyle(24)
@Composable fun stomic28() = stomicStyle(28)
@Composable fun stomic35() = stomicStyle(35)
@Composable fun stomic48() = stomicStyle(48)
@Composable fun stomic54() = stomicStyle(54)
@Composable fun stomic72() = stomicStyle(72)
