package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun LocalFindsScreenContent(
    state: LocalFindsState,
    onIntent: (LocalFindsIntent) -> Unit,
    onFindClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load legends", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        color = colors.colorOrangeMain,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onIntent(LocalFindsIntent.Load) }
                    )
                }
            }
        }

        state.items.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No legends yet.", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorSurface),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(state.items, key = { _, item -> item.id }) { _, item ->
                    LocalFindCard(
                        item = item,
                        lang = lang,
                        onClick = { onFindClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalFindCard(item: LocalFind, lang: String, onClick: () -> Unit) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.09f)
            )
            .clip(shape)
            .background(colors.colorWhite)
            .clickable(onClick = onClick)
    ) {
        // Full-bleed hero photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(colors.colorSlateGhost)
        ) {
            if (item.photos.isNotEmpty()) {
                AsyncImage(
                    model = item.photos.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Content
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Category label
            Text(
                text = "Kotor Legend",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                ),
                color = colors.colorAmberDark
            )

            // Title
            Text(
                text = item.localizedTitle(lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                ),
                color = colors.colorNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Body preview
            val body = item.localizedBody(lang)
            if (body.isNotBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = colors.colorSlate,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Location row
            val location = item.localizedLocation(lang)
            if (location.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.colorAmber)
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.colorSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
