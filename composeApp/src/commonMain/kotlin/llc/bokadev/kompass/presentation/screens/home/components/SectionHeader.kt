package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.ui.theme.KompassTheme

@Composable
fun SectionHeader(
    title: String,
    onSeeAll: (() -> Unit)?,
    titleColor: Color = KompassTheme.colors.white,
    labelColor: Color = titleColor,
    localSurfaceColor: Color? = null
) {
    val strings = rememberAppStrings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .let {
                if (localSurfaceColor != null) {
                    it.clip(RoundedCornerShape(14.dp))
                        .background(localSurfaceColor)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                } else it
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = KompassTheme.typography.helveticaBold24.copy(fontSize = 22.sp, lineHeight = 28.sp),
            color = titleColor
        )
        if (onSeeAll != null) {
            Row(
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.seeAll,
                    style = KompassTheme.typography.helveticaMedium13,
                    color = labelColor
                )
            }
        }
    }
}
