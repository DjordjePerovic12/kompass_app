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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.domain.model.UtilityCategory

/** Stroke glyphs for the Essentials screen (decorative; the surrounding control carries the label). */
enum class EssentialsGlyph {
    Document, Pin, Clock, Warning, Phone, Bus, Wallet, Lightbulb, ChevronDown, ArrowUpRight,
    Map, Plus, Cash, Cross, Cart, Bag, Parking, Fuel
}

@Composable
fun EssentialsIcon(glyph: EssentialsGlyph, color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (glyph) {
            EssentialsGlyph.Document -> {
                rect(color, s, 0.24f, 0.12f, 0.52f, 0.76f)
                line(color, s, 0.36f, 0.4f, 0.64f, 0.4f); line(color, s, 0.36f, 0.56f, 0.64f, 0.56f)
            }
            EssentialsGlyph.Pin -> {
                val p = Path().apply {
                    moveTo(w(0.5f), h(0.9f))
                    cubicTo(w(0.2f), h(0.56f), w(0.2f), h(0.38f), w(0.2f), h(0.38f))
                    cubicTo(w(0.2f), h(0.08f), w(0.8f), h(0.08f), w(0.8f), h(0.38f))
                    cubicTo(w(0.8f), h(0.56f), w(0.5f), h(0.9f), w(0.5f), h(0.9f))
                }
                drawPath(p, color, style = s)
                drawCircle(color, radius = this.size.minDimension * 0.09f, center = Offset(w(0.5f), h(0.38f)), style = s)
            }
            EssentialsGlyph.Clock -> {
                drawCircle(color, radius = this.size.minDimension * 0.4f, center = center, style = s)
                line(color, s, 0.5f, 0.5f, 0.5f, 0.28f); line(color, s, 0.5f, 0.5f, 0.66f, 0.58f)
            }
            EssentialsGlyph.Warning -> {
                val p = Path().apply { moveTo(w(0.5f), h(0.1f)); lineTo(w(0.92f), h(0.88f)); lineTo(w(0.08f), h(0.88f)); close() }
                drawPath(p, color, style = s)
                line(color, s, 0.5f, 0.4f, 0.5f, 0.62f)
                drawCircle(color, radius = s.width * 0.6f, center = Offset(w(0.5f), h(0.74f)))
            }
            EssentialsGlyph.Phone -> {
                val p = Path().apply {
                    moveTo(w(0.22f), h(0.14f)); lineTo(w(0.38f), h(0.14f)); lineTo(w(0.46f), h(0.34f)); lineTo(w(0.36f), h(0.42f))
                    quadraticTo(w(0.46f), h(0.58f), w(0.6f), h(0.66f)); lineTo(w(0.68f), h(0.56f)); lineTo(w(0.88f), h(0.64f))
                    lineTo(w(0.88f), h(0.8f)); quadraticTo(w(0.86f), h(0.88f), w(0.78f), h(0.88f))
                    quadraticTo(w(0.14f), h(0.84f), w(0.12f), h(0.22f)); quadraticTo(w(0.12f), h(0.14f), w(0.22f), h(0.14f)); close()
                }
                drawPath(p, color, style = s)
            }
            EssentialsGlyph.Bus -> {
                rect(color, s, 0.2f, 0.14f, 0.6f, 0.62f)
                line(color, s, 0.2f, 0.48f, 0.8f, 0.48f)
                line(color, s, 0.32f, 0.76f, 0.32f, 0.88f); line(color, s, 0.68f, 0.76f, 0.68f, 0.88f)
            }
            EssentialsGlyph.Wallet -> {
                rect(color, s, 0.12f, 0.24f, 0.76f, 0.54f)
                line(color, s, 0.12f, 0.4f, 0.88f, 0.4f); line(color, s, 0.62f, 0.62f, 0.74f, 0.62f)
            }
            EssentialsGlyph.Lightbulb -> {
                drawArc(color, 140f, 260f, false, Offset(w(0.2f), h(0.08f)), Size(w(0.6f), h(0.6f)), style = s)
                line(color, s, 0.38f, 0.62f, 0.38f, 0.74f); line(color, s, 0.62f, 0.62f, 0.62f, 0.74f)
                line(color, s, 0.38f, 0.74f, 0.62f, 0.74f); line(color, s, 0.42f, 0.88f, 0.58f, 0.88f)
            }
            EssentialsGlyph.ChevronDown -> {
                val p = Path().apply { moveTo(w(0.24f), h(0.38f)); lineTo(w(0.5f), h(0.64f)); lineTo(w(0.76f), h(0.38f)) }
                drawPath(p, color, style = s)
            }
            EssentialsGlyph.ArrowUpRight -> {
                line(color, s, 0.24f, 0.76f, 0.76f, 0.24f)
                val p = Path().apply { moveTo(w(0.36f), h(0.24f)); lineTo(w(0.76f), h(0.24f)); lineTo(w(0.76f), h(0.64f)) }
                drawPath(p, color, style = s)
            }
            EssentialsGlyph.Map -> {
                val p = Path().apply {
                    moveTo(w(0.1f), h(0.22f)); lineTo(w(0.37f), h(0.12f)); lineTo(w(0.63f), h(0.24f)); lineTo(w(0.9f), h(0.14f))
                    lineTo(w(0.9f), h(0.78f)); lineTo(w(0.63f), h(0.88f)); lineTo(w(0.37f), h(0.76f)); lineTo(w(0.1f), h(0.86f)); close()
                }
                drawPath(p, color, style = s)
                line(color, s, 0.37f, 0.12f, 0.37f, 0.76f); line(color, s, 0.63f, 0.24f, 0.63f, 0.88f)
            }
            EssentialsGlyph.Plus -> {
                val thick = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                line(color, thick, 0.5f, 0.2f, 0.5f, 0.8f); line(color, thick, 0.2f, 0.5f, 0.8f, 0.5f)
            }
            EssentialsGlyph.Cash -> {
                rect(color, s, 0.1f, 0.26f, 0.8f, 0.48f)
                drawCircle(color, radius = this.size.minDimension * 0.1f, center = center, style = s)
            }
            EssentialsGlyph.Cross -> {
                rect(color, s, 0.14f, 0.14f, 0.72f, 0.72f)
                line(color, s, 0.5f, 0.32f, 0.5f, 0.68f); line(color, s, 0.32f, 0.5f, 0.68f, 0.5f)
            }
            EssentialsGlyph.Cart -> {
                val p = Path().apply { moveTo(w(0.1f), h(0.2f)); lineTo(w(0.24f), h(0.2f)); lineTo(w(0.34f), h(0.62f)); lineTo(w(0.8f), h(0.62f)); lineTo(w(0.88f), h(0.32f)); lineTo(w(0.28f), h(0.32f)) }
                drawPath(p, color, style = s)
                drawCircle(color, radius = this.size.minDimension * 0.05f, center = Offset(w(0.4f), h(0.8f)), style = s)
                drawCircle(color, radius = this.size.minDimension * 0.05f, center = Offset(w(0.74f), h(0.8f)), style = s)
            }
            EssentialsGlyph.Bag -> {
                rect(color, s, 0.18f, 0.34f, 0.64f, 0.54f)
                drawArc(color, 180f, 180f, false, Offset(w(0.34f), h(0.16f)), Size(w(0.32f), h(0.36f)), style = s)
            }
            EssentialsGlyph.Parking -> {
                rect(color, s, 0.14f, 0.14f, 0.72f, 0.72f)
                val p = Path().apply {
                    moveTo(w(0.4f), h(0.72f)); lineTo(w(0.4f), h(0.3f)); lineTo(w(0.54f), h(0.3f))
                    cubicTo(w(0.7f), h(0.3f), w(0.7f), h(0.52f), w(0.54f), h(0.52f)); lineTo(w(0.4f), h(0.52f))
                }
                drawPath(p, color, style = s)
            }
            EssentialsGlyph.Fuel -> {
                rect(color, s, 0.2f, 0.16f, 0.4f, 0.72f)
                line(color, s, 0.2f, 0.42f, 0.6f, 0.42f)
                val p = Path().apply { moveTo(w(0.6f), h(0.3f)); lineTo(w(0.76f), h(0.42f)); lineTo(w(0.76f), h(0.74f)) }
                drawPath(p, color, style = s)
            }
        }
    }
}

fun UtilityCategory.glyph(): EssentialsGlyph = when (this) {
    UtilityCategory.ATM -> EssentialsGlyph.Cash
    UtilityCategory.PHARMACY -> EssentialsGlyph.Cross
    UtilityCategory.SUPERMARKET -> EssentialsGlyph.Cart
    UtilityCategory.SHOP -> EssentialsGlyph.Bag
    UtilityCategory.PARKING -> EssentialsGlyph.Parking
    UtilityCategory.GAS_STATION -> EssentialsGlyph.Fuel
    UtilityCategory.EMERGENCY -> EssentialsGlyph.Plus
}

private fun DrawScope.w(f: Float) = size.width * f
private fun DrawScope.h(f: Float) = size.height * f
private fun DrawScope.line(color: Color, s: Stroke, x1: Float, y1: Float, x2: Float, y2: Float) =
    drawLine(color, Offset(w(x1), h(y1)), Offset(w(x2), h(y2)), strokeWidth = s.width, cap = StrokeCap.Round)
private fun DrawScope.rect(color: Color, s: Stroke, x: Float, y: Float, width: Float, height: Float) =
    drawRoundRect(color, Offset(w(x), h(y)), Size(w(width), h(height)), CornerRadius(2.dp.toPx()), style = s)
