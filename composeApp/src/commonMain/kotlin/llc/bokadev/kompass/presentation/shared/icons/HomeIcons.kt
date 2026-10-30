package llc.bokadev.kompass.presentation.shared.icons

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A plain right-pointing arrow (stem + head, Material's arrow_forward shape). */
@Composable
fun ArrowRightIcon(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.5.dp.toPx()
        val midY = h * 0.5f
        drawLine(
            color = color,
            start = Offset(w * 0.12f, midY),
            end = Offset(w * 0.8f, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val head = Path().apply {
            moveTo(w * 0.52f, h * 0.22f)
            lineTo(w * 0.85f, midY)
            lineTo(w * 0.52f, h * 0.78f)
        }
        drawPath(head, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** A plain downward chevron (Material's expand_more shape). */
@Composable
fun ChevronDownIcon(color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.6.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.36f)
            lineTo(w * 0.5f, h * 0.66f)
            lineTo(w * 0.82f, h * 0.36f)
        }
        drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** A plain checkmark (Material's check shape). */
@Composable
fun CheckIcon(color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.78f)
            lineTo(w * 0.86f, h * 0.26f)
        }
        drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** A plain magnifying-glass search icon. */
@Composable
fun SearchIcon(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.7.dp.toPx()
        val radius = this.size.minDimension * 0.3f
        val center = Offset(w * 0.42f, h * 0.42f)
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
        val handleStart = Offset(center.x + radius * 0.74f, center.y + radius * 0.74f)
        drawLine(
            color = color,
            start = handleStart,
            end = Offset(w * 0.92f, h * 0.92f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/** A plain left-pointing chevron (back navigation). */
@Composable
fun ChevronLeftIcon(color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.64f, h * 0.18f)
            lineTo(w * 0.32f, h * 0.5f)
            lineTo(w * 0.64f, h * 0.82f)
        }
        drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** A plain filled play triangle. */
@Composable
fun PlayIcon(color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.18f)
            lineTo(w * 0.28f, h * 0.82f)
            lineTo(w * 0.82f, h * 0.5f)
            close()
        }
        drawPath(path, color)
    }
}

/** A globe — circle + one vertical meridian ellipse + one horizontal latitude line. */
@Composable
fun GlobeIcon(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.5.dp.toPx()
        val radius = this.size.minDimension / 2f - stroke
        val center = Offset(w / 2f, h / 2f)
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
        drawOval(
            color = color,
            topLeft = Offset(center.x - radius * 0.42f, center.y - radius),
            size = Size(radius * 0.84f, radius * 2f),
            style = Stroke(width = stroke)
        )
        drawLine(
            color = color,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = stroke
        )
    }
}

/** A bookmark — outline when [filled] is false, solid fill when true (saved state). */
@Composable
fun BookmarkIcon(filled: Boolean, color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.08f)
            lineTo(w * 0.75f, h * 0.08f)
            lineTo(w * 0.75f, h * 0.92f)
            lineTo(w * 0.5f, h * 0.68f)
            lineTo(w * 0.25f, h * 0.92f)
            close()
        }
        if (filled) {
            drawPath(path, color)
        } else {
            drawPath(path, color, style = Stroke(width = 1.7.dp.toPx(), join = StrokeJoin.Round))
        }
    }
}

/** A plain plus sign. */
@Composable
fun PlusIcon(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.8.dp.toPx()
        drawLine(color, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.12f, h * 0.5f), Offset(w * 0.88f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/** A filled warning triangle outline with an exclamation mark. */
@Composable
fun WarningTriangleIcon(color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.6.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.94f, h * 0.88f)
            lineTo(w * 0.06f, h * 0.88f)
            close()
        }
        drawPath(path, color, style = Stroke(width = stroke, join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawLine(color, Offset(w * 0.5f, h * 0.4f), Offset(w * 0.5f, h * 0.62f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(color, radius = stroke * 0.55f, center = Offset(w * 0.5f, h * 0.74f))
    }
}

/** A circled "i" for general/informational notices. */
@Composable
fun InfoCircleIcon(color: Color, size: Dp = 16.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.6.dp.toPx()
        val radius = this.size.minDimension / 2f - stroke
        val center = Offset(w / 2f, h / 2f)
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
        drawCircle(color, radius = stroke * 0.55f, center = Offset(center.x, center.y - radius * 0.38f))
        drawLine(color, Offset(center.x, center.y - radius * 0.05f), Offset(center.x, center.y + radius * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/** Over-ear headphones: a top arc plus two ear cups. */
@Composable
fun HeadphonesIcon(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.7.dp.toPx()
        val band = Path().apply {
            moveTo(w * 0.16f, h * 0.62f)
            lineTo(w * 0.16f, h * 0.5f)
            cubicTo(w * 0.16f, h * 0.06f, w * 0.84f, h * 0.06f, w * 0.84f, h * 0.5f)
            lineTo(w * 0.84f, h * 0.62f)
        }
        drawPath(band, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val cupW = w * 0.2f
        val cupH = h * 0.32f
        val cupTop = h * 0.56f
        val radius = CornerRadius(cupW * 0.4f)
        drawRoundRect(color, topLeft = Offset(w * 0.1f, cupTop), size = Size(cupW, cupH), cornerRadius = radius)
        drawRoundRect(color, topLeft = Offset(w * 0.9f - cupW, cupTop), size = Size(cupW, cupH), cornerRadius = radius)
    }
}

/** An outline landmark (pediment, three columns, base) for the missing-photo block. */
@Composable
fun LandmarkIcon(color: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.6.dp.toPx()
        val style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val roof = Path().apply {
            moveTo(w * 0.1f, h * 0.36f)
            lineTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.9f, h * 0.36f)
            close()
        }
        drawPath(roof, color, style = style)
        listOf(0.24f, 0.5f, 0.76f).forEach { x ->
            drawLine(color, Offset(w * x, h * 0.46f), Offset(w * x, h * 0.76f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
        drawLine(color, Offset(w * 0.1f, h * 0.88f), Offset(w * 0.9f, h * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
