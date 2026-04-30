package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun InfoChip(
    label: String,
    amber: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (amber) colors.colorAmberSubtle else Color.Transparent)
            .border(
                width = if (amber) 0.dp else 1.dp,
                color = if (amber) Color.Transparent else colors.colorSlateGhost,
                shape = shape
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (amber) colors.colorAmberDark else colors.colorSlate
        )
    }
}
