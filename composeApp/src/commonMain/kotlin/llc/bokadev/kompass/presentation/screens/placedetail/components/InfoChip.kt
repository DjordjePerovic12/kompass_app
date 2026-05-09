package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun InfoChip(
    label: String,
    amber: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (amber) colors.colorOrangeMain.copy(alpha = 0.08f) else colors.colorSurface)
            .border(
                width = 1.dp,
                color = if (amber) colors.colorOrangeMain.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.08f),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            if (amber) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.colorOrangeMain)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (amber) colors.colorOrangeMain else colors.colorNavy.copy(alpha = 0.82f)
            )
        }
    }
}
