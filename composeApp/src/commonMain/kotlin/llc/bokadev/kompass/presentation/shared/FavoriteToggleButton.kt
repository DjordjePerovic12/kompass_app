package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun FavoriteToggleButton(
    isFavorited: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    val colors = KompassTheme.colors
    val iconColor = if (isFavorited) Color(0xFFE53935) else colors.colorNavy.copy(alpha = 0.48f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                ambientColor = colors.colorNavy.copy(alpha = 0.05f),
                spotColor = colors.colorNavy.copy(alpha = 0.08f)
            )
            .clip(CircleShape)
            .background(colors.colorWhite)
            .border(1.dp, colors.colorNavy.copy(alpha = 0.08f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val path = heartPath(this.size.width, this.size.height)
            if (isFavorited) {
                drawPath(path, color = iconColor)
            } else {
                drawPath(
                    path,
                    color = iconColor,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// Material Design heart path, scaled to the given canvas dimensions (viewport 24×24).
internal fun heartPath(w: Float, h: Float): Path {
    val sx = w / 24f
    val sy = h / 24f
    return Path().apply {
        moveTo(12f * sx, 21.35f * sy)
        lineTo(10.55f * sx, 20.03f * sy)
        cubicTo(5.4f * sx, 15.36f * sy, 2f * sx, 12.28f * sy, 2f * sx, 8.5f * sy)
        cubicTo(2f * sx, 5.42f * sy, 4.42f * sx, 3f * sy, 7.5f * sx, 3f * sy)
        cubicTo(9.24f * sx, 3f * sy, 10.91f * sx, 3.81f * sy, 12f * sx, 5.09f * sy)
        cubicTo(13.09f * sx, 3.81f * sy, 14.76f * sx, 3f * sy, 16.5f * sx, 3f * sy)
        cubicTo(19.58f * sx, 3f * sy, 22f * sx, 5.42f * sy, 22f * sx, 8.5f * sy)
        cubicTo(22f * sx, 12.28f * sy, 18.6f * sx, 15.36f * sy, 13.45f * sx, 20.04f * sy)
        close()
    }
}
