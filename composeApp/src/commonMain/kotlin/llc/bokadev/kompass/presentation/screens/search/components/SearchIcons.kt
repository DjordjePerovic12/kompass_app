package llc.bokadev.kompass.presentation.screens.search.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.ui.theme.KompassTheme

/** Stroke glyphs only Search needs (decorative; the surrounding control carries the label). */
enum class SearchGlyph { Clock, Close, Book, SearchMinus }

@Composable
fun SearchGlyphIcon(glyph: SearchGlyph, color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(color, Offset(w * x1, h * y1), Offset(w * x2, h * y2), stroke.width, StrokeCap.Round)
        when (glyph) {
            SearchGlyph.Clock -> {
                drawCircle(color, radius = w * 0.4f, style = stroke)
                line(0.5f, 0.28f, 0.5f, 0.5f); line(0.5f, 0.5f, 0.66f, 0.6f)
            }
            SearchGlyph.Close -> { line(0.28f, 0.28f, 0.72f, 0.72f); line(0.72f, 0.28f, 0.28f, 0.72f) }
            SearchGlyph.Book -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.26f)
                    cubicTo(w * 0.4f, h * 0.18f, w * 0.24f, h * 0.18f, w * 0.12f, h * 0.22f)
                    lineTo(w * 0.12f, h * 0.8f)
                    cubicTo(w * 0.24f, h * 0.76f, w * 0.4f, h * 0.76f, w * 0.5f, h * 0.84f)
                    cubicTo(w * 0.6f, h * 0.76f, w * 0.76f, h * 0.76f, w * 0.88f, h * 0.8f)
                    lineTo(w * 0.88f, h * 0.22f)
                    cubicTo(w * 0.76f, h * 0.18f, w * 0.6f, h * 0.18f, w * 0.5f, h * 0.26f)
                    close()
                }
                drawPath(p, color, style = stroke)
                line(0.5f, 0.26f, 0.5f, 0.84f)
            }
            SearchGlyph.SearchMinus -> {
                drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.44f, h * 0.44f), style = stroke)
                line(0.66f, 0.66f, 0.88f, 0.88f)
                line(0.32f, 0.44f, 0.56f, 0.44f)
            }
        }
    }
}

@Preview
@Composable
private fun SearchGlyphIconPreview() {
    SearchGlyphIcon(SearchGlyph.Book, KompassTheme.colors.lampYellow, 24.dp)
}
