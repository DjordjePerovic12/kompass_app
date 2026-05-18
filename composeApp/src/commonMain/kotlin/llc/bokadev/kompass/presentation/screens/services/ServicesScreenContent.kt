package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun ServicesScreenContent(
    state: ServicesState,
    onIntent: (ServicesEvent) -> Unit,
    onServiceClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val strings = rememberAppStrings()

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load services", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = strings.retry,
                        color = colors.colorOrangeMain,
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
                    .background(colors.colorWhite),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.services, key = { it.id }) { service ->
                    ServiceListCard(
                        service = service,
                        onClick = { onServiceClick(service.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceListCard(
    service: Service,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val location = service.localizedLocation(lang)
    val meta = listOfNotNull(
        location?.takeIf { it.isNotBlank() },
        service.externalWebsite?.let { "Website available" }
    ).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.colorWhite)
            .border(1.dp, colors.colorSignal.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(104.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.colorOrangeMain.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(34.dp)) { drawServiceIcon(colors.colorOrangeMain) }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.colorSignal.copy(alpha = 0.22f))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Service${location?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = colors.colorSignalStrong.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = service.localizedName(lang),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        lineHeight = 24.sp
                    ),
                    color = colors.colorNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = service.localizedDescription(lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.colorSlateLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlateLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

internal fun DrawScope.drawServiceIcon(color: Color) {
    val w = size.width
    val h = size.height
    drawCircle(
        color = color,
        radius = w * 0.22f,
        center = Offset(w * 0.28f, h * 0.28f),
        style = Stroke(width = 2.dp.toPx())
    )
    val path = Path().apply {
        moveTo(w * 0.42f, h * 0.42f)
        lineTo(w * 0.82f, h * 0.82f)
        lineTo(w * 0.75f, h * 0.88f)
        lineTo(w * 0.35f, h * 0.48f)
        close()
    }
    drawPath(path, color)
}
