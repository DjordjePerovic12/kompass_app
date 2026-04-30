package llc.bokadev.kompass.presentation.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.KompassTheme

enum class BottomTab(val label: String) {
    Home("Home"),
    Categories("Categories"),
    Events("Events"),
    Experiences("Experiences")
}

@Composable
fun KompassBottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val colors = KompassTheme.colors
    NavigationBar(
        containerColor = colors.colorWhite,
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .height(64.dp)
            .drawBehind {
                drawLine(
                    color = colors.colorSlateGhost,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val iconColor = if (selected) colors.colorNavy else colors.colorSlateLight
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { TabIcon(tab = tab, color = iconColor) },
                label = if (selected) {
                    { Text(tab.label, style = MaterialTheme.typography.labelSmall) }
                } else null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.colorNavy,
                    selectedTextColor = colors.colorNavy,
                    indicatorColor = colors.colorAmberSubtle,
                    unselectedIconColor = colors.colorSlateLight,
                    unselectedTextColor = colors.colorSlateLight
                )
            )
        }
    }
}

@Composable
private fun TabIcon(tab: BottomTab, color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        when (tab) {
            BottomTab.Home        -> drawHome(color)
            BottomTab.Categories  -> drawCategories(color)
            BottomTab.Events      -> drawEvents(color)
            BottomTab.Experiences -> drawExperiences(color)
        }
    }
}

private fun DrawScope.drawHome(color: Color) {
    val w = size.width
    val h = size.height
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.04f, h * 0.48f)
        lineTo(w * 0.18f, h * 0.48f)
        lineTo(w * 0.18f, h * 0.92f)
        lineTo(w * 0.82f, h * 0.92f)
        lineTo(w * 0.82f, h * 0.48f)
        lineTo(w * 0.96f, h * 0.48f)
        close()
    }
    drawPath(path, color)
    drawRect(
        color = Color.White,
        topLeft = Offset(w * 0.39f, h * 0.58f),
        size = Size(w * 0.22f, h * 0.34f)
    )
}

private fun DrawScope.drawCategories(color: Color) {
    val cell = size.width * 0.38f
    val gap = size.width * 0.12f
    listOf(
        Offset(gap, gap),
        Offset(cell + gap * 2, gap),
        Offset(gap, cell + gap * 2),
        Offset(cell + gap * 2, cell + gap * 2)
    ).forEach { offset ->
        drawRoundRect(
            color = color,
            topLeft = offset,
            size = Size(cell, cell),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
    }
}

private fun DrawScope.drawEvents(color: Color) {
    val w = size.width
    val h = size.height
    val cr = CornerRadius(w * 0.1f)
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.08f, h * 0.14f),
        size = Size(w * 0.84f, h * 0.78f),
        cornerRadius = cr
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(w * 0.08f, h * 0.35f),
        size = Size(w * 0.84f, h * 0.57f)
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.08f, h * 0.7f),
        size = Size(w * 0.84f, h * 0.22f),
        cornerRadius = cr
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.08f, h * 0.7f),
        size = Size(w * 0.84f, h * 0.22f),
        cornerRadius = cr
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.08f, h * 0.78f),
        size = Size(w * 0.84f, h * 0.15f),
        cornerRadius = CornerRadius(0f)
    )
    drawRoundRect(color = color, topLeft = Offset(w * 0.28f, h * 0.02f), size = Size(w * 0.12f, h * 0.2f), cornerRadius = CornerRadius(w * 0.06f))
    drawRoundRect(color = color, topLeft = Offset(w * 0.60f, h * 0.02f), size = Size(w * 0.12f, h * 0.2f), cornerRadius = CornerRadius(w * 0.06f))
    drawCircle(color = color, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.66f))
}

private fun DrawScope.drawExperiences(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerR = size.width * 0.45f
    val innerR = size.width * 0.19f
    val path = androidx.compose.ui.graphics.Path()
    val points = 5
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = kotlin.math.PI * i / points - kotlin.math.PI / 2
        val x = cx + r * kotlin.math.cos(angle).toFloat()
        val y = cy + r * kotlin.math.sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

