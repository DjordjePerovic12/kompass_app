package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.presentation.theme.KOmpassTheme
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun
        FilterChip(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    filter: EventFilter,
    lang: String = "en",
    onFilterSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize()
            .clip(RoundedCornerShape(100.dp))
            .background(if (isSelected) KompassTheme.colors.colorNavy else KompassTheme.colors.filterUnselectedSurface )
            .padding(12.dp)
            .noRippleClickable {
                onFilterSelected(filter.key)
            }

    ) {
        Text(
            text = filter.localizedLabel(lang),
            color = if (isSelected) Color.White else KompassTheme.colors.filterUnselectedText
        )
    }
}
