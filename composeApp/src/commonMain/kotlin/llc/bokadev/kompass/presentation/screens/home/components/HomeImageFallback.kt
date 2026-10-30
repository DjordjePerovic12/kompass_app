package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * Intentional compact text-led treatment for a missing or failed photo — never a blank
 * gray photograph or an invented replacement image. [secondaryLabel], when given, renders
 * as a smaller line below the title (e.g. category/zone metadata), so a card falling back to
 * this treatment doesn't lose information a photo card would otherwise show.
 */
@Composable
fun HomeImageFallback(label: String, secondaryLabel: String? = null, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(KompassTheme.colors.lavender),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = KompassTheme.typography.helveticaMedium16.copy(fontSize = 15.sp, lineHeight = 20.sp),
                color = KompassTheme.colors.inkBlack,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!secondaryLabel.isNullOrBlank()) {
                Text(
                    text = secondaryLabel,
                    style = KompassTheme.typography.helveticaRegular14.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = KompassTheme.colors.inkBlack.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
