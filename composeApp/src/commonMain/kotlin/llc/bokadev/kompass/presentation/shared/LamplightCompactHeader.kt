package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.screens.home.components.ChevronLeftIcon
import llc.bokadev.kompass.presentation.screens.home.components.SearchIcon
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * The yellow header for second-level Lamplight screens (category lists): a back button, an
 * optional search button, a Gloock title and a one-line subtitle.
 */
@Composable
fun LamplightCompactHeader(
    title: String,
    subtitle: String?,
    backLabel: String,
    onBack: () -> Unit,
    searchLabel: String? = null,
    onSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.lamp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.night)
                    .clickable(onClick = onBack)
                    .semantics { role = Role.Button; contentDescription = backLabel },
                contentAlignment = Alignment.Center
            ) { ChevronLeftIcon(color = colors.lamp, size = 18.dp) }
            Spacer(Modifier.weight(1f))
            if (onSearch != null) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, colors.night, CircleShape)
                        .clickable(onClick = onSearch)
                        .semantics { role = Role.Button; contentDescription = searchLabel.orEmpty() },
                    contentAlignment = Alignment.Center
                ) { SearchIcon(color = colors.night, size = 18.dp) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = LamplightType.areaTitle().copy(fontSize = 44.sp, lineHeight = 44.sp),
                color = colors.night,
                modifier = Modifier.semantics { heading() }
            )
            subtitle?.let {
                Text(text = it, style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = colors.night)
            }
        }
    }
}

@Preview
@Composable
private fun LamplightCompactHeaderPreview() {
    LamplightCompactHeader("Places", "Heritage and viewpoints", "Back", {}, "Search", {})
}
