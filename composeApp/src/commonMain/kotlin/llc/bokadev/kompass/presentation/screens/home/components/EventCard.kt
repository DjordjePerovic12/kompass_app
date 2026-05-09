package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun EventCard(
    name: String,
    venue: String,
    category: String,
    day: String,
    month: String,
    meta: String? = null,
    price: String? = null,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val cardShape = RoundedCornerShape(16.dp)
    val locationLine = listOfNotNull(
        venue.takeIf { it.isNotBlank() },
        meta?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape, ambientColor = colors.colorNavy.copy(alpha = 0.06f), spotColor = colors.colorNavy.copy(alpha = 0.08f))
            .clip(cardShape)
            .background(colors.colorWhite)
            .border(1.dp, colors.colorNavy.copy(alpha = 0.06f), cardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.colorOrangeMain),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.colorWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = month.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.colorWhite.copy(alpha = 0.9f)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.colorNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            price?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.colorOrangeMain
                )
            }

            if (locationLine.isNotBlank()) {
                Text(
                    text = locationLine,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    color = colors.colorSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.colorWhite)
                .border(1.dp, colors.colorNavy.copy(alpha = 0.08f), CircleShape)
                .clickable(onClick = onFavoriteClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Add to favorites",
                tint = colors.colorNavy.copy(alpha = 0.48f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
