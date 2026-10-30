package llc.bokadev.kompass.presentation.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.theme.LamplightColors
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

enum class BottomTab {
    Home,
    Explore,
    Saved,
    Essentials
}

/**
 * "Lamplight" floating bottom bar (spec §3.6). Five equal slots — the four real tabs plus the
 * center [audioButton] (see `NavAudioButton`), which is *not* a tab: it never takes part in
 * selection or back-stack behavior. Global chrome shared by all 4 tabs
 * (not Home-exclusive) — restyled in place, same as the rest of this component's history.
 */
@Composable
fun KompassBottomNavBar(
    /** Null when no tab is current (the audio library, reached from the center button). */
    selectedTab: BottomTab?,
    onTabSelected: (BottomTab) -> Unit,
    audioButton: @Composable () -> Unit
) {
    val strings = rememberAppStrings()
    val colors = LamplightTheme.colors
    val shellShape = LamplightShapes.nav

    // The pill's own fill extends through the system nav-bar/home-indicator inset
    // (navigationBarsPadding is applied *inside* the shadow/clip/background chain, not outside
    // it) so the bar's shape physically covers that strip — no separate margin is left for a
    // platform background to show through underneath it.
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .padding(horizontal = 14.dp)
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // The pill is its own layer behind the row, so the center button's pulse rings and hint
        // bubble can draw past the bar's edge instead of being clipped by its shape.
        Box(
            modifier = Modifier
                .matchParentSize()
                // Step 7.9: a lighter lift (was 10dp at 30/45%); the pill's own colour separates it.
                .shadow(
                    elevation = 4.dp,
                    shape = shellShape,
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(shellShape)
                .background(colors.navBar)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            // Bottom: every label (including the taller center Listen button's) on one baseline.
            verticalAlignment = Alignment.Bottom
        ) {
            BottomNavItem(tab = BottomTab.Home, label = strings.bottomHome, selected = selectedTab == BottomTab.Home, colors = colors, onClick = { onTabSelected(BottomTab.Home) })
            BottomNavItem(tab = BottomTab.Explore, label = strings.redesign.explore, selected = selectedTab == BottomTab.Explore, colors = colors, onClick = { onTabSelected(BottomTab.Explore) })
            audioButton()
            BottomNavItem(tab = BottomTab.Saved, label = strings.redesign.saved, selected = selectedTab == BottomTab.Saved, colors = colors, onClick = { onTabSelected(BottomTab.Saved) })
            BottomNavItem(tab = BottomTab.Essentials, label = strings.bottomEssentials, selected = selectedTab == BottomTab.Essentials, colors = colors, onClick = { onTabSelected(BottomTab.Essentials) })
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomTab,
    label: String,
    selected: Boolean,
    colors: LamplightColors,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(min = 56.dp)
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.chalk),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawTabIcon(tab, colors.night)
                }
            }
        } else {
            Canvas(modifier = Modifier.size(34.dp).padding(7.dp)) {
                drawTabIcon(tab, colors.textTertiary)
            }
        }

        Text(
            text = label,
            style = LamplightType.navLabel(selected = selected),
            color = if (selected) colors.chalk else colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawTabIcon(tab: BottomTab, color: Color) {
    when (tab) {
        BottomTab.Home -> drawHome(color)
        BottomTab.Explore -> drawCompass(color)
        BottomTab.Saved -> drawSaved(color)
        BottomTab.Essentials -> drawLifebuoy(color)
    }
}

private fun DrawScope.drawHome(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.1f)
        lineTo(w * 0.14f, h * 0.42f)
        lineTo(w * 0.14f, h * 0.86f)
        lineTo(w * 0.86f, h * 0.86f)
        lineTo(w * 0.86f, h * 0.42f)
        close()
    }
    drawPath(path, color, style = Stroke(width = 1.9.dp.toPx()))
    drawRect(
        color = color,
        topLeft = Offset(w * 0.44f, h * 0.58f),
        size = Size(w * 0.12f, h * 0.22f)
    )
}

/** A compass glyph — ring + a kite-shaped needle (spec §1.4: "compass (Explore)"). */
private fun DrawScope.drawCompass(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 1.8.dp.toPx()
    val center = Offset(w * 0.5f, h * 0.5f)
    val radius = w * 0.38f
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
    val needle = Path().apply {
        moveTo(center.x, center.y - radius * 0.55f)
        lineTo(center.x + radius * 0.24f, center.y)
        lineTo(center.x, center.y + radius * 0.55f)
        lineTo(center.x - radius * 0.24f, center.y)
        close()
    }
    drawPath(needle, color, style = Stroke(width = stroke * 0.8f))
}

private fun DrawScope.drawSaved(color: Color) {
    val w = size.width
    val h = size.height
    drawPath(Path().apply {
        moveTo(w * 0.25f, h * 0.12f)
        lineTo(w * 0.75f, h * 0.12f)
        lineTo(w * 0.75f, h * 0.88f)
        lineTo(w * 0.5f, h * 0.66f)
        lineTo(w * 0.25f, h * 0.88f)
        close()
    }, color, style = Stroke(width = 1.9.dp.toPx()))
}

/** A life-ring glyph — two concentric rings + four tick marks (spec §1.4: "lifebuoy (Essentials)"). */
private fun DrawScope.drawLifebuoy(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 1.7.dp.toPx()
    val center = Offset(w * 0.5f, h * 0.5f)
    val outerR = w * 0.38f
    val innerR = w * 0.19f
    drawCircle(color = color, radius = outerR, center = center, style = Stroke(width = stroke))
    drawCircle(color = color, radius = innerR, center = center, style = Stroke(width = stroke))
    drawLine(color, Offset(center.x, center.y - outerR), Offset(center.x, center.y - innerR), strokeWidth = stroke)
    drawLine(color, Offset(center.x, center.y + innerR), Offset(center.x, center.y + outerR), strokeWidth = stroke)
    drawLine(color, Offset(center.x - outerR, center.y), Offset(center.x - innerR, center.y), strokeWidth = stroke)
    drawLine(color, Offset(center.x + innerR, center.y), Offset(center.x + outerR, center.y), strokeWidth = stroke)
}

/** A headphones glyph — headband arc + two ear cups (spec §1.4: "headphones (audio)"). */
internal fun DrawScope.drawHeadphones(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 1.9.dp.toPx()
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.18f, h * 0.08f),
        size = Size(w * 0.64f, h * 0.64f),
        style = Stroke(width = stroke, cap = StrokeCap.Round)
    )
    val cupSize = Size(w * 0.2f, h * 0.36f)
    val cupRadius = CornerRadius(5.dp.toPx())
    drawRoundRect(color = color, topLeft = Offset(w * 0.12f, h * 0.46f), size = cupSize, cornerRadius = cupRadius, style = Stroke(width = stroke))
    drawRoundRect(color = color, topLeft = Offset(w * 0.68f, h * 0.46f), size = cupSize, cornerRadius = cupRadius, style = Stroke(width = stroke))
}
