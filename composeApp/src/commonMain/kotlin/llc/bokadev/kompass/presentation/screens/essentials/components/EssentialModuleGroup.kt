package llc.bokadev.kompass.presentation.screens.essentials.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon
import llc.bokadev.kompass.presentation.shared.icons.glyph

/** One action under an expanded module: an exact-coordinate map handoff or an external link. */
data class ModuleAction(val glyph: EssentialsGlyph, val title: String, val actionLabel: String, val onClick: () -> Unit)

/** One accordion row, already localized. [subtitle] is the problem it solves, or null to hide it. */
data class ModuleItem(
    val id: String,
    val title: String,
    val subtitle: String?,
    val body: String,
    val actions: List<ModuleAction>
)

data class ModuleLabels(val readMore: String, val showLess: String, val expanded: String, val collapsed: String)

/**
 * A topic's accordion (spec §2.5): rows on one raised container with inset dividers. Several
 * rows can be open at once; expansion state is owned by the caller so it survives tab switches.
 */
@Composable
fun EssentialModuleGroup(
    items: List<ModuleItem>,
    expandedIds: Set<String>,
    labels: ModuleLabels,
    reduceMotion: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.charcoal)) {
        items.forEachIndexed { index, item ->
            if (index > 0) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.slateDivider))
            ModuleRow(item, item.id in expandedIds, labels, reduceMotion) { onToggle(item.id) }
        }
    }
}

@Composable
private fun ModuleRow(item: ModuleItem, expanded: Boolean, labels: ModuleLabels, reduceMotion: Boolean, onToggle: () -> Unit) {
    val colors = KompassTheme.colors
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, if (reduceMotion) snap() else tween(200), label = "chevron")
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (expanded) KompassTheme.colors.slateDeep else Color.Transparent)
            .then(if (reduceMotion) Modifier else Modifier.animateContentSize())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(onClick = onToggle)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    stateDescription = if (expanded) labels.expanded else labels.collapsed
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = item.title, style = KompassTheme.typography.geologicaSemiBold17, color = if (expanded) colors.lampYellow else colors.boneWhite)
                item.subtitle?.let {
                    Text(text = it, style = KompassTheme.typography.geologicaRegular13, color = colors.pebbleGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            EssentialsIcon(EssentialsGlyph.ChevronDown, color = colors.pebbleGray, size = 20.dp, modifier = Modifier.graphicsLayer { rotationZ = rotation })
        }
        if (expanded) ModuleBody(item, labels)
    }
}

@Composable
private fun ModuleBody(item: ModuleItem, labels: ModuleLabels) {
    val colors = KompassTheme.colors
    var showAll by remember(item.id) { mutableStateOf(false) }
    var long by remember(item.id) { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (item.body.isNotBlank()) {
            Text(
                text = item.body,
                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
                color = colors.boneWhite,
                maxLines = if (showAll || !long) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
                // More than ~8 lines collapses to 6 + Read more (spec §2.5).
                onTextLayout = { if (!showAll && !long && it.lineCount > 8) long = true }
            )
            if (long) {
                Text(
                    text = if (showAll) labels.showLess else labels.readMore,
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.lampYellow,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clickable { showAll = !showAll }
                        .padding(vertical = 12.dp)
                )
            }
        }
        item.actions.forEach { action ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.slateDark)
                    .clickable(onClick = action.onClick)
                    .semantics(mergeDescendants = true) { role = Role.Button }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EssentialsIcon(action.glyph, color = colors.lampYellow, size = 20.dp)
                Box(Modifier.width(12.dp))
                Text(
                    text = action.title,
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.boneWhite,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = action.actionLabel, style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold), color = colors.lampYellow)
            }
        }
    }
}

@Preview
@Composable
private fun EssentialModuleGroupPreview() {
    EssentialModuleGroup(
        items = listOf(
            ModuleItem("taxi", "Taxi", null, "Only Red Taxi and Djir Taxi are legit.", listOf(ModuleAction(EssentialsGlyph.ArrowUpRight, "redtaxikotor.com", "Open") {})),
            ModuleItem("bus", "Main Bus Station", null, "", emptyList())
        ),
        expandedIds = setOf("taxi"),
        labels = ModuleLabels("Read more", "Show less", "Expanded", "Collapsed"),
        reduceMotion = false,
        onToggle = {}
    )
}
