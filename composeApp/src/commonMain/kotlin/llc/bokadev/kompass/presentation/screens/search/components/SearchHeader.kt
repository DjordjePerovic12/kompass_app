package llc.bokadev.kompass.presentation.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.screens.home.components.ChevronLeftIcon
import llc.bokadev.kompass.presentation.screens.home.components.SearchIcon
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * The search field *is* the header (Search spec §2.1): a `lamp` bar with Back and a 52dp `night`
 * field. Clear keeps focus in the field, so the keyboard stays up.
 */
@Composable
fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    backLabel: String,
    clearLabel: String,
    onBack: () -> Unit,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colors = LamplightTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.lamp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.night)
                .clickable(onClick = onBack)
                .semantics { role = Role.Button; contentDescription = backLabel },
            contentAlignment = Alignment.Center
        ) { ChevronLeftIcon(color = colors.lamp, size = 18.dp) }

        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .clip(CircleShape)
                .background(colors.night)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchIcon(color = colors.lamp, size = 20.dp)
            Box(Modifier.weight(1f).padding(vertical = 14.dp), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(placeholder, style = LamplightType.body().copy(fontSize = 16.sp), color = colors.textTertiary, maxLines = 1)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = LamplightType.body().copy(fontSize = 16.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.lamp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics { contentDescription = placeholder }
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceMuted)
                        .clickable {
                            onQueryChange("")
                            runCatching { focusRequester.requestFocus() }
                        }
                        .semantics { role = Role.Button; contentDescription = clearLabel },
                    contentAlignment = Alignment.Center
                ) { SearchGlyphIcon(SearchGlyph.Close, colors.textPrimary, 16.dp) }
            } else {
                Box(Modifier.height(36.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SearchHeaderPreview() {
    SearchHeader("pima", {}, "Search Kotor and the bay", "Back", "Clear search", {}, {})
}
