package llc.bokadev.kompass.presentation.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.theme.KompassTheme

enum class BottomTab {
    Home,
    Categories,
    Activities,
    Essentials
}

@Composable
fun KompassBottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val colors = KompassTheme.colors
    val strings = rememberAppStrings()
    val shellShape = RoundedCornerShape(34.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 0.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = shellShape,
                    ambientColor = colors.colorNavy.copy(alpha = 0.14f),
                    spotColor = colors.colorNavy.copy(alpha = 0.18f)
                )
                .clip(shellShape)
                .background(colors.colorWhite)
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val label = when (tab) {
                    BottomTab.Home -> strings.bottomHome
                    BottomTab.Categories -> strings.bottomBrowse
                    BottomTab.Activities -> strings.bottomActivities
                    BottomTab.Essentials -> strings.bottomEssentials
                }

                BottomNavItem(
                    tab = tab,
                    label = label,
                    selected = selected,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomTab,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .widthIn(min = 62.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        ambientColor = colors.colorNavy.copy(alpha = 0.08f),
                        spotColor = colors.colorNavy.copy(alpha = 0.10f)
                    )
                    .clip(CircleShape)
                    .background(colors.colorSignal.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(25.dp)) {
                    drawTabIcon(tab = tab, color = colors.colorSignalStrong)
                }
            }
        } else {
            Canvas(modifier = Modifier.size(24.dp)) {
                drawTabIcon(tab = tab, color = colors.colorSlateSoft.copy(alpha = 0.72f))
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                letterSpacing = 0.05.sp
            ),
            color = if (selected) colors.colorSignalStrong else colors.colorSlateSoft.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTabIcon(tab: BottomTab, color: Color) {
    when (tab) {
        BottomTab.Home -> drawHome(color)
        BottomTab.Categories -> drawCategories(color)
        BottomTab.Activities -> drawActivities(color)
        BottomTab.Essentials -> drawProfile(color)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHome(color: Color) {
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCategories(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 1.8.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.12f, h * 0.18f),
        size = Size(w * 0.26f, h * 0.22f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = stroke)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.56f, h * 0.18f),
        size = Size(w * 0.18f, h * 0.18f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = stroke)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.12f, h * 0.56f),
        size = Size(w * 0.18f, h * 0.18f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = stroke)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.42f, h * 0.50f),
        size = Size(w * 0.32f, h * 0.24f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = stroke)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawActivities(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 1.9.dp.toPx()
    drawCircle(
        color = color,
        radius = w * 0.13f,
        center = Offset(w * 0.28f, h * 0.36f),
        style = Stroke(width = stroke)
    )
    drawCircle(
        color = color,
        radius = w * 0.13f,
        center = Offset(w * 0.72f, h * 0.36f),
        style = Stroke(width = stroke)
    )
    drawCircle(
        color = color,
        radius = w * 0.13f,
        center = Offset(w * 0.28f, h * 0.72f),
        style = Stroke(width = stroke)
    )
    drawCircle(
        color = color,
        radius = w * 0.13f,
        center = Offset(w * 0.72f, h * 0.72f),
        style = Stroke(width = stroke)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProfile(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 1.9.dp.toPx()
    drawCircle(
        color = color,
        radius = w * 0.18f,
        center = Offset(w * 0.5f, h * 0.32f),
        style = Stroke(width = stroke)
    )
    drawPath(
        path = Path().apply {
            moveTo(w * 0.22f, h * 0.84f)
            cubicTo(w * 0.30f, h * 0.60f, w * 0.70f, h * 0.60f, w * 0.78f, h * 0.84f)
        },
        color = color,
        style = Stroke(width = stroke)
    )
}
