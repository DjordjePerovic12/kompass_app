package llc.bokadev.kompass.presentation.search.components

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * One search result (Search spec §2.3): a 56dp tile (photo, else the type icon — never an empty
 * box), the type label, the name with the first match in `lamp` 700, a context line and a chevron.
 * The whole row is one target; there is no save button.
 */
@Composable
fun SearchResultRow(
    typeLabel: String,
    name: String,
    matchStart: Int,
    matchLength: Int,
    context: String?,
    photoUrl: String?,
    essential: Boolean,
    icon: @Composable (Color) -> Unit,
    chevron: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val description = listOfNotNull(typeLabel, name, context).joinToString(", ")
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp)
                .clickable(onClick = onClick)
                .clearAndSetSemantics { role = Role.Button; contentDescription = description }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            var photoFailed by remember(photoUrl) { mutableStateOf(false) }
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(colors.slateDark),
                contentAlignment = Alignment.Center
            ) {
                icon(colors.lampYellow)
                if (photoUrl != null && !photoFailed) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onError = { photoFailed = true },
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = typeLabel,
                    style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 10.sp),
                    color = if (essential) colors.stoneGray else colors.lampYellow
                )
                Text(
                    text = highlighted(name, matchStart, matchLength, colors.lampYellow),
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
                    color = colors.boneWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                context?.let {
                    Text(text = it, style = KompassTheme.typography.geologicaRegular13, color = colors.pebbleGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            chevron(colors.pebbleGray)
        }
        Box(Modifier.fillMaxWidth().padding(start = 20.dp).height(1.dp).background(colors.slateDivider))
    }
}

private fun highlighted(name: String, start: Int, length: Int, color: Color): AnnotatedString {
    if (start < 0 || length <= 0 || start + length > name.length) return AnnotatedString(name)
    return buildAnnotatedString {
        append(name.substring(0, start))
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(name.substring(start, start + length)) }
        append(name.substring(start + length))
    }
}

@Preview
@Composable
private fun SearchResultRowPreview() {
    SearchResultRow(
        typeLabel = "PLACE", name = "Pima Palace", matchStart = 0, matchLength = 2, context = "Palace · Old Town",
        photoUrl = null, essential = false,
        icon = { SearchGlyphIcon(SearchGlyph.Book, it, 24.dp) }, chevron = { SearchGlyphIcon(SearchGlyph.Close, it, 14.dp) },
        onClick = {}
    )
}
