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
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.language_close
import kompass.composeapp.generated.resources.language_current
import kompass.composeapp.generated.resources.language_names_de
import kompass.composeapp.generated.resources.language_names_en
import kompass.composeapp.generated.resources.language_names_es
import kompass.composeapp.generated.resources.language_names_fr
import kompass.composeapp.generated.resources.language_names_tr
import kompass.composeapp.generated.resources.language_subtitle
import kompass.composeapp.generated.resources.language_title
import kompass.composeapp.generated.resources.language_use_phone_language
import llc.bokadev.kompass.core.utils.SUPPORTED_LANGUAGES
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import llc.bokadev.kompass.presentation.shared.icons.CheckIcon
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.icons.DetailIcon
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** Purple accents for the selected language, from the page gradient's family (owner's call over lamp yellow). */

/**
 * The language picker as a bottom sheet on the app's purple page gradient: each language by its
 * own name, the current one marked, and a "use my phone's language" shortcut. Replaces the old
 * full-screen Change Language page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    currentLanguage: String,
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
        LanguageChooser(currentLanguage, onSelect, onUsePhoneLanguage, onDismiss)
    }
}

/**
 * The language list itself, shared by the sheet and the first-launch screen. [onDismiss] null
 * hides the close button (first launch has nothing to close back to).
 */
@Composable
internal fun LanguageChooser(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onUsePhoneLanguage: () -> Unit,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val closeLabel = stringResource(Res.string.language_close)
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
                    text = stringResource(Res.string.language_title),
                    style = KompassTheme.typography.gloockRegular32.copy(fontSize = 40.sp, lineHeight = 42.sp),
                    color = Color.White,
                    modifier = Modifier.semantics { heading() }
                )
                Text(text = stringResource(Res.string.language_subtitle), style = KompassTheme.typography.geologicaRegular14.copy(fontSize = 15.sp, lineHeight = 21.sp), color = KompassTheme.colors.white85)
            }
            if (onDismiss != null) Box(
                Modifier
                    .padding(start = 12.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(KompassTheme.colors.inkBlack.copy(alpha = 0.35f))
                    .clickable(onClick = onDismiss)
                    .semantics { role = Role.Button; contentDescription = closeLabel },
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
                .background(KompassTheme.colors.inkBlack.copy(alpha = 0.55f))
                .selectableGroup()
        ) {
            SUPPORTED_LANGUAGES.forEachIndexed { index, (code, endonym) ->
                if (index > 0) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.slateDivider))
                val selected = code == currentLanguage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .background(if (selected) KompassTheme.colors.periwinkle.copy(alpha = 0.22f) else Color.Transparent)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(code) })
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) KompassTheme.colors.periwinkle else colors.slateDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = code.uppercase(),
                            style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = if (selected) Color.White else colors.stoneGray
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = endonym, style = KompassTheme.typography.geologicaSemiBold17, color = if (selected) KompassTheme.colors.lavenderLight else colors.boneWhite)
                        Text(
                            text = if (selected) stringResource(Res.string.language_current) else languageNameRes(code)?.let { stringResource(it) }.orEmpty(),
                            style = KompassTheme.typography.geologicaRegular14,
                            color = colors.stoneGray
                        )
                    }
                    if (selected) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(KompassTheme.colors.periwinkle), contentAlignment = Alignment.Center) {
                            CheckIcon(color = Color.White, size = 14.dp)
                        }
                    } else {
                        Box(Modifier.size(26.dp).border(1.5.dp, colors.pebbleGray, CircleShape))
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
            DetailIcon(DetailGlyph.Phone, color = KompassTheme.colors.lavenderLight, size = 18.dp)
            Text(text = stringResource(Res.string.language_use_phone_language), style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold), color = KompassTheme.colors.lavenderLight)
        }
    }
}

/** A language's name in the current UI language (e.g. "German" in English), null for an unknown code. */
private fun languageNameRes(code: String): StringResource? = when (code) {
    "en" -> Res.string.language_names_en
    "de" -> Res.string.language_names_de
    "fr" -> Res.string.language_names_fr
    "es" -> Res.string.language_names_es
    "tr" -> Res.string.language_names_tr
    else -> null
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
    LanguageChooser("en", {}, {}, {})
}
