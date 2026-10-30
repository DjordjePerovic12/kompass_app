package llc.bokadev.kompass.presentation.shared.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.DetailStrings
import llc.bokadev.kompass.core.utils.detailStringsFor
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.icons.DetailIcon

/**
 * Body text on a detail page, collapsed after [collapsedLines] with Read more / Show less
 * (spec §1.6). An optional Gloock [title] heads it (e.g. "How to get there").
 */
@Composable
fun DetailTextSection(
    text: String,
    strings: DetailStrings,
    modifier: Modifier = Modifier,
    title: String? = null,
    collapsedLines: Int = 6
) {
    if (text.isBlank()) return
    val colors = KompassTheme.colors
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    var overflows by remember(text) { mutableStateOf(false) }

    Column(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        title?.let {
            Text(text = it, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite, modifier = Modifier.semantics { heading() })
        }
        Column {
            Text(
                text = text,
                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.Normal, lineHeight = 26.sp),
                color = colors.boneWhite,
                maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
            )
            if (overflows || expanded) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clickable { expanded = !expanded }
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            stateDescription = if (expanded) strings.expanded else strings.collapsed
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (expanded) strings.showLess else strings.readMore,
                        style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.lampYellow
                    )
                    DetailIcon(if (expanded) DetailGlyph.ChevronUp else DetailGlyph.ChevronDown, color = colors.lampYellow, size = 16.dp)
                }
            }
        }
    }
}

@Preview
@Composable
private fun DetailTextSectionPreview() {
    DetailTextSection("Take the old road up from the sea gate and follow the switchbacks.", detailStringsFor("en"), title = "How to get there")
}
