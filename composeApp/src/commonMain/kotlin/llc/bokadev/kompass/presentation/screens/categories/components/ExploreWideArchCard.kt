package llc.bokadev.kompass.presentation.screens.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

private val ArchShape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)

/**
 * Worth the Drive (spec §2.4): text on the left, a photo arch bottom-right with a hard `night`
 * shadow, the same arch language as Home's Nearby card. No photo (or a failed one) hides the arch
 * and the text takes the full width.
 */
@Composable
fun ExploreWideArchCard(
    label: String,
    title: String,
    subtitle: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    val shape = RoundedCornerShape(24.dp)
    var failed by remember(imageUrl) { mutableStateOf(false) }
    val showArch = !imageUrl.isNullOrBlank() && !failed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clip(shape)
            .background(colors.surfaceRaised)
            .border(1.dp, colors.divider, shape)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
                onClick { onClick(); true }
            }
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label.uppercase(), style = LamplightType.label(), color = colors.lamp)
            Text(text = title, style = LamplightType.sectionTitleS(), color = colors.textPrimary)
            Text(text = subtitle, style = LamplightType.bodyS().copy(fontSize = 14.sp), color = colors.textSecondary)
        }
        if (showArch) {
            Box(Modifier.width(132.dp).height(150.dp), contentAlignment = Alignment.BottomEnd) {
                Box(Modifier.size(width = 112.dp, height = 128.dp).offset(x = (-8).dp).background(colors.night, ArchShape))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = { failed = true },
                    modifier = Modifier.size(width = 112.dp, height = 128.dp).clip(ArchShape).background(colors.surfaceMuted)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExploreWideArchCardPreview() {
    ExploreWideArchCard("Further out", "Worth the Drive", "Further into the region", null, {})
}
