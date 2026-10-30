package llc.bokadev.kompass.presentation.explore.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.ui.theme.KompassTheme

/** Stroke glyphs used by the Explore cards (decorative; callers pass no content description). */
enum class ExploreGlyph { ForkKnife, Walker, Calendar, Info, Wrench, ChevronRight, ArrowRight }

@Composable
fun ExploreIcon(glyph: ExploreGlyph, color: Color, size: Dp = 28.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (glyph) {
            ExploreGlyph.ForkKnife -> forkKnife(color, stroke)
            ExploreGlyph.Walker -> walker(color, stroke)
            ExploreGlyph.Calendar -> calendar(color, stroke)
            ExploreGlyph.Info -> info(color, stroke)
            ExploreGlyph.Wrench -> wrench(color, stroke)
            ExploreGlyph.ChevronRight -> chevronRight(color, stroke)
            ExploreGlyph.ArrowRight -> arrowRight(color, stroke)
        }
    }
}

private fun DrawScope.line(color: Color, stroke: Stroke, x1: Float, y1: Float, x2: Float, y2: Float) =
    drawLine(color, Offset(size.width * x1, size.height * y1), Offset(size.width * x2, size.height * y2), strokeWidth = stroke.width, cap = StrokeCap.Round)

private fun DrawScope.forkKnife(color: Color, stroke: Stroke) {
    // Fork: three tines joined, then a handle.
    listOf(0.2f, 0.3f, 0.4f).forEach { x -> line(color, stroke, x, 0.12f, x, 0.36f) }
    drawArc(color, 0f, 180f, false, Offset(size.width * 0.2f, size.height * 0.26f), Size(size.width * 0.2f, size.height * 0.2f), style = stroke)
    line(color, stroke, 0.3f, 0.46f, 0.3f, 0.88f)
    // Knife: a blade on the right, then a handle.
    val blade = Path().apply {
        moveTo(size.width * 0.66f, size.height * 0.88f)
        lineTo(size.width * 0.66f, size.height * 0.12f)
        quadraticTo(size.width * 0.82f, size.height * 0.22f, size.width * 0.8f, size.height * 0.52f)
        lineTo(size.width * 0.66f, size.height * 0.52f)
    }
    drawPath(blade, color, style = stroke)
}

private fun DrawScope.walker(color: Color, stroke: Stroke) {
    drawCircle(color, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.56f, size.height * 0.14f), style = stroke)
    line(color, stroke, 0.54f, 0.28f, 0.46f, 0.56f)   // torso
    line(color, stroke, 0.46f, 0.56f, 0.34f, 0.88f)   // back leg
    line(color, stroke, 0.46f, 0.56f, 0.6f, 0.7f)     // front thigh
    line(color, stroke, 0.6f, 0.7f, 0.62f, 0.88f)     // front shin
    line(color, stroke, 0.52f, 0.34f, 0.34f, 0.46f)   // back arm
    line(color, stroke, 0.52f, 0.34f, 0.7f, 0.44f)    // front arm
}

private fun DrawScope.calendar(color: Color, stroke: Stroke) {
    drawRoundRect(color, Offset(size.width * 0.16f, size.height * 0.22f), Size(size.width * 0.68f, size.height * 0.62f), CornerRadius(3.dp.toPx()), style = stroke)
    line(color, stroke, 0.16f, 0.4f, 0.84f, 0.4f)
    line(color, stroke, 0.34f, 0.14f, 0.34f, 0.28f)
    line(color, stroke, 0.66f, 0.14f, 0.66f, 0.28f)
}

private fun DrawScope.info(color: Color, stroke: Stroke) {
    val r = size.minDimension * 0.38f
    drawCircle(color, radius = r, center = center, style = stroke)
    drawCircle(color, radius = stroke.width * 0.6f, center = Offset(center.x, center.y - r * 0.42f))
    line(color, stroke, 0.5f, 0.48f, 0.5f, 0.68f)
}

private fun DrawScope.wrench(color: Color, stroke: Stroke) {
    line(color, stroke, 0.22f, 0.78f, 0.56f, 0.44f)
    drawArc(color, -20f, 280f, false, Offset(size.width * 0.5f, size.height * 0.16f), Size(size.width * 0.34f, size.height * 0.34f), style = stroke)
}

private fun DrawScope.chevronRight(color: Color, stroke: Stroke) {
    val path = Path().apply {
        moveTo(size.width * 0.38f, size.height * 0.22f)
        lineTo(size.width * 0.64f, size.height * 0.5f)
        lineTo(size.width * 0.38f, size.height * 0.78f)
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.arrowRight(color: Color, stroke: Stroke) {
    line(color, stroke, 0.2f, 0.5f, 0.8f, 0.5f)
    val head = Path().apply {
        moveTo(size.width * 0.54f, size.height * 0.24f)
        lineTo(size.width * 0.8f, size.height * 0.5f)
        lineTo(size.width * 0.54f, size.height * 0.76f)
    }
    drawPath(head, color, style = stroke)
}

@Preview
@Composable
private fun ExploreIconPreview() {
    KompassTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExploreGlyph.entries.forEach { ExploreIcon(it, KompassTheme.colors.lampYellow) }
        }
    }
}
