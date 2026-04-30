package llc.bokadev.kompass.presentation.screens.languagepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel

private val languages = listOf(
    "en" to "English",
    "fr" to "Français",
    "tr" to "Türkçe",
    "es" to "Español",
    "de" to "Deutsch"
)

@Composable
fun LanguagePickerScreen(onLanguageSelected: () -> Unit) {
    val vm: LanguagePickerViewModel = koinViewModel()
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorSurface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("K")
                withStyle(SpanStyle(color = colors.colorAmber)) {
                    append("Ompass")
                }
            },
            style = MaterialTheme.typography.displayLarge,
            color = colors.colorNavy
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Choose your language",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        languages.forEach { (code, label) ->
            LanguageButton(label = label) {
                vm.onIntent(LanguagePickerEvent.SelectLanguage(code))
                onLanguageSelected()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LanguageButton(label: String, onClick: () -> Unit) {
    val colors = KompassTheme.colors
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = colors.colorWhite,
            contentColor = colors.colorNavy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = colors.colorNavy
            )
        }
    }
}
