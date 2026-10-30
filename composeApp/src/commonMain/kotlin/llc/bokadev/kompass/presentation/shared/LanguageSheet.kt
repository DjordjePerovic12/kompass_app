package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.LanguageStrings
import llc.bokadev.kompass.core.utils.SUPPORTED_LANGUAGES
import llc.bokadev.kompass.core.utils.languageStringsFor
import llc.bokadev.kompass.presentation.screens.home.components.CheckIcon
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailIcon
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** Purple accents for the selected language, from the page gradient's family (owner's call over lamp yellow). */
private val AccentFill = HomePalette.action
private val AccentText = HomePalette.canvas

/**
 * The language picker as a bottom sheet on the app's purple page gradient: each language by its
 * own name, the current one marked, and a "use my phone's language" shortcut. Replaces the old
 * full-screen Change Language page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    currentLanguage: String,
    strings: LanguageStrings,
    onSelect: (String) -> Unit,
    onUsePhoneLanguage: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SheetShape,
        containerColor = Color.Transparent,
        dragHandle = null,
        // The gradient runs behind the system navigation bar; the body pads itself above it.
        contentWindowInsets = { WindowInsets(0) }
    ) {
        LanguageChooser(currentLanguage, strings, onSelect, onUsePhoneLanguage, onDismiss)
    }
}

/**
 * The language list itself, shared by the sheet and the first-launch screen. [onDismiss] null
 * hides the close button (first launch has nothing to close back to).
 */
@Composable
internal fun LanguageChooser(
    currentLanguage: String,
    strings: LanguageStrings,
    onSelect: (String) -> Unit,
    onUsePhoneLanguage: () -> Unit,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SheetShape)
            .pageGradientBackground()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp)
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
        )
        Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.title,
                    style = LamplightType.sectionTitle().copy(fontSize = 40.sp, lineHeight = 42.sp),
                    color = Color.White,
                    modifier = Modifier.semantics { heading() }
                )
                Text(text = strings.subtitle, style = LamplightType.bodyS().copy(fontSize = 15.sp, lineHeight = 21.sp), color = HomePalette.onGradientSecondary)
            }
            if (onDismiss != null) Box(
                Modifier
                    .padding(start = 12.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HomePalette.ink.copy(alpha = 0.35f))
                    .clickable(onClick = onDismiss)
                    .semantics { role = Role.Button; contentDescription = strings.close },
                contentAlignment = Alignment.Center
            ) {
                CloseGlyph()
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(HomePalette.ink.copy(alpha = 0.55f))
                .selectableGroup()
        ) {
            SUPPORTED_LANGUAGES.forEachIndexed { index, (code, endonym) ->
                if (index > 0) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.divider))
                val selected = code == currentLanguage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .background(if (selected) AccentFill.copy(alpha = 0.22f) else Color.Transparent)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(code) })
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) AccentFill else colors.surfaceMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = code.uppercase(),
                            style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else colors.textSecondary
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = endonym, style = LamplightType.bodyL(), color = if (selected) AccentText else colors.textPrimary)
                        Text(
                            text = if (selected) strings.current else strings.names[code].orEmpty(),
                            style = LamplightType.bodyS(),
                            color = colors.textSecondary
                        )
                    }
                    if (selected) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(AccentFill), contentAlignment = Alignment.Center) {
                            CheckIcon(color = Color.White, size = 14.dp)
                        }
                    } else {
                        Box(Modifier.size(26.dp).border(1.5.dp, colors.textTertiary, CircleShape))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onUsePhoneLanguage)
                .semantics(mergeDescendants = true) { role = Role.Button }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailIcon(DetailGlyph.Phone, color = AccentText, size = 18.dp)
            Text(text = strings.usePhoneLanguage, style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold), color = AccentText)
        }
    }
}

@Composable
private fun CloseGlyph() {
    Canvas(Modifier.size(16.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Preview
@Composable
private fun LanguageSheetPreview() {
    LanguageChooser("en", languageStringsFor("en"), {}, {}, {})
}
