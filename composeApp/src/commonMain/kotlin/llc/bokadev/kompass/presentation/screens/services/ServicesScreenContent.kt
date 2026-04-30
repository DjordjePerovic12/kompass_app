package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.buildMapsUrl
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun ServicesScreenContent(
    state: ServicesState,
    onIntent: (ServicesEvent) -> Unit
) {
    val colors = KompassTheme.colors

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load services", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        color = colors.colorAmberDark,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onIntent(ServicesEvent.Retry) }
                    )
                }
            }
        }

        state.services.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No services available", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorWhite)
            ) {
                itemsIndexed(state.services, key = { _, s -> s.id }) { index, service ->
                    ServiceRow(service = service)
                    if (index < state.services.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                            color = colors.colorSlateGhost
                        )
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ServiceRow(service: Service) {
    val colors = KompassTheme.colors
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.colorAmberSubtle),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(22.dp)) { drawServiceIcon(colors.colorAmberDark) }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = service.localizedName("en"),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
            Text(
                text = service.localizedDescription("en"),
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorSlate,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            service.localizedLocation("en")?.let { location ->
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.clickable { uriHandler.openUri(buildMapsUrl(location)) },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) { drawPin(colors.colorAmberDark) }
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.colorAmberDark
                    )
                }
            }

            service.externalWebsite?.let { url ->
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.colorAmberSubtle)
                        .clickable { uriHandler.openUri(url) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) { drawExternalLink(colors.colorAmberDark) }
                    Text(
                        text = "Visit website",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.colorAmberDark
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawServiceIcon(color: Color) {
    val w = size.width
    val h = size.height
    // wrench shape
    drawCircle(color = color, radius = w * 0.22f, center = Offset(w * 0.28f, h * 0.28f), style = Stroke(width = 2.dp.toPx()))
    val path = Path().apply {
        moveTo(w * 0.42f, h * 0.42f)
        lineTo(w * 0.82f, h * 0.82f)
        lineTo(w * 0.75f, h * 0.88f)
        lineTo(w * 0.35f, h * 0.48f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawPin(color: Color) {
    val cx = size.width / 2f
    drawCircle(color = color, radius = size.width * 0.3f, center = Offset(cx, size.height * 0.32f))
    val path = Path().apply {
        moveTo(cx - size.width * 0.18f, size.height * 0.42f)
        lineTo(cx, size.height * 0.92f)
        lineTo(cx + size.width * 0.18f, size.height * 0.42f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawExternalLink(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 1.5.dp.toPx())
    // box with arrow
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.05f, h * 0.3f),
        size = androidx.compose.ui.geometry.Size(w * 0.55f, h * 0.65f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.1f),
        style = stroke
    )
    val arrow = Path().apply {
        moveTo(w * 0.55f, h * 0.05f)
        lineTo(w * 0.95f, h * 0.05f)
        lineTo(w * 0.95f, h * 0.45f)
        moveTo(w * 0.95f, h * 0.05f)
        lineTo(w * 0.45f, h * 0.55f)
    }
    drawPath(arrow, color, style = stroke)
}
