package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun KompassSharedTopBar(
    modifier: Modifier = Modifier,
    slug: String,
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.colorNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
            .padding(top = if (showBack) 12.dp else 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(16.dp)) { drawBackArrow(Color.White) }
            }
            Box(modifier = Modifier.padding(top = 12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = slug,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }
        } else {
            Text(
                text = slug,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }
    }
}

private fun DrawScope.drawBackArrow(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.65f, size.height * 0.15f)
        lineTo(size.width * 0.25f, size.height * 0.5f)
        lineTo(size.width * 0.65f, size.height * 0.85f)
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}
