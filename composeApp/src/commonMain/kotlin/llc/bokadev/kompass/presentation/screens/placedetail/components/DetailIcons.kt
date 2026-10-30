package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Stroke icons for the place detail screen, drawn like Home's HomeIcons (no icon dependency).

/** Which practical-row / action glyph to draw. */
enum class DetailGlyph { Clock, Price, Visit, Sun, ArrowUpRight, Phone, Lightbulb, ChevronDown, ChevronUp, Landmark }

@Composable
fun DetailIcon(glyph: DetailGlyph, color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (glyph) {
            DetailGlyph.Clock -> drawClock(color, stroke)
            DetailGlyph.Price -> drawPriceLines(color, stroke)
            DetailGlyph.Visit -> drawSparkle(color, stroke)
            DetailGlyph.Sun -> drawSun(color, stroke)
            DetailGlyph.ArrowUpRight -> drawArrowUpRight(color, stroke)
            DetailGlyph.Phone -> drawPhone(color, stroke)
            DetailGlyph.Lightbulb -> drawLightbulb(color, stroke)
            DetailGlyph.ChevronDown -> drawChevron(color, stroke, down = true)
            DetailGlyph.ChevronUp -> drawChevron(color, stroke, down = false)
            DetailGlyph.Landmark -> drawLandmark(color, stroke)
        }
    }
}

private fun DrawScope.drawClock(color: Color, stroke: Stroke) {
    val r = size.minDimension * 0.42f
    drawCircle(color, radius = r, center = center, style = stroke)
    drawLine(color, center, Offset(center.x, center.y - r * 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(color, center, Offset(center.x + r * 0.42f, center.y + r * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

private fun DrawScope.drawPriceLines(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    listOf(0.28f to 0.82f, 0.5f to 0.82f, 0.72f to 0.58f).forEach { (y, end) ->
        drawLine(color, Offset(w * 0.18f, h * y), Offset(w * end, h * y), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawSparkle(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.1f)
        quadraticTo(w * 0.56f, h * 0.44f, w * 0.9f, h * 0.5f)
        quadraticTo(w * 0.56f, h * 0.56f, w * 0.5f, h * 0.9f)
        quadraticTo(w * 0.44f, h * 0.56f, w * 0.1f, h * 0.5f)
        quadraticTo(w * 0.44f, h * 0.44f, w * 0.5f, h * 0.1f)
        close()
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.drawSun(color: Color, stroke: Stroke) {
    val r = size.minDimension * 0.2f
    drawCircle(color, radius = r, center = center, style = stroke)
    repeat(8) { i ->
        val angle = i * PI / 4
        val inner = r + size.minDimension * 0.1f
        val outer = r + size.minDimension * 0.22f
        drawLine(
            color,
            Offset(center.x + (cos(angle) * inner).toFloat(), center.y + (sin(angle) * inner).toFloat()),
            Offset(center.x + (cos(angle) * outer).toFloat(), center.y + (sin(angle) * outer).toFloat()),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawArrowUpRight(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawLine(color, Offset(w * 0.22f, h * 0.78f), Offset(w * 0.78f, h * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    val head = Path().apply {
        moveTo(w * 0.36f, h * 0.22f)
        lineTo(w * 0.78f, h * 0.22f)
        lineTo(w * 0.78f, h * 0.64f)
    }
    drawPath(head, color, style = stroke)
}

private fun DrawScope.drawPhone(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.22f, h * 0.14f)
        lineTo(w * 0.38f, h * 0.14f)
        lineTo(w * 0.46f, h * 0.34f)
        lineTo(w * 0.36f, h * 0.42f)
        quadraticTo(w * 0.46f, h * 0.58f, w * 0.6f, h * 0.66f)
        lineTo(w * 0.68f, h * 0.56f)
        lineTo(w * 0.88f, h * 0.64f)
        lineTo(w * 0.88f, h * 0.8f)
        quadraticTo(w * 0.86f, h * 0.88f, w * 0.78f, h * 0.88f)
        quadraticTo(w * 0.14f, h * 0.84f, w * 0.12f, h * 0.22f)
        quadraticTo(w * 0.12f, h * 0.14f, w * 0.22f, h * 0.14f)
        close()
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.drawLightbulb(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawArc(color, startAngle = 140f, sweepAngle = 260f, useCenter = false, topLeft = Offset(w * 0.2f, h * 0.08f), size = Size(w * 0.6f, h * 0.6f), style = stroke)
    drawLine(color, Offset(w * 0.38f, h * 0.62f), Offset(w * 0.38f, h * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.62f, h * 0.62f), Offset(w * 0.62f, h * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.38f, h * 0.74f), Offset(w * 0.62f, h * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.42f, h * 0.88f), Offset(w * 0.58f, h * 0.88f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

private fun DrawScope.drawChevron(color: Color, stroke: Stroke, down: Boolean) {
    val w = size.width
    val h = size.height
    val (a, b) = if (down) 0.38f to 0.64f else 0.62f to 0.36f
    val path = Path().apply {
        moveTo(w * 0.22f, h * a)
        lineTo(w * 0.5f, h * b)
        lineTo(w * 0.78f, h * a)
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.drawLandmark(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val roof = Path().apply {
        moveTo(w * 0.1f, h * 0.36f)
        lineTo(w * 0.5f, h * 0.12f)
        lineTo(w * 0.9f, h * 0.36f)
        close()
    }
    drawPath(roof, color, style = stroke)
    listOf(0.24f, 0.5f, 0.76f).forEach { x ->
        drawLine(color, Offset(w * x, h * 0.46f), Offset(w * x, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
    drawLine(color, Offset(w * 0.1f, h * 0.88f), Offset(w * 0.9f, h * 0.88f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
