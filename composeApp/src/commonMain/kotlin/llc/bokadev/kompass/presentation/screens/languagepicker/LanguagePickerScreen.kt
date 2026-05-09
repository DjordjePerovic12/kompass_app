package llc.bokadev.kompass.presentation.screens.languagepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.core.util.supportedLanguages
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguagePickerScreen(
    onLanguageSelected: () -> Unit,
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    val vm: LanguagePickerViewModel = koinViewModel()
    val colors = KompassTheme.colors
    val strings = rememberAppStrings()
    val selectedLanguage = currentAppLanguage()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorHomeCanvas)
    ) {
        KompassSharedTopBar(
            slug = strings.changeLanguage,
            title = strings.chooseLanguage,
            subtitle = strings.chooseLanguageSubtitle,
            showBack = showBack,
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 18.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = colors.colorWhite,
                        contentColor = colors.colorNavy
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        supportedLanguages.forEachIndexed { index, option ->
                            LanguageRow(
                                label = option.label,
                                isSelected = option.code == selectedLanguage
                            ) {
                                vm.onIntent(LanguagePickerEvent.SelectLanguage(option.code))
                                onLanguageSelected()
                            }

                            if (index != supportedLanguages.lastIndex) {
                                HorizontalDivider(
                                    color = colors.colorSurfaceMid.copy(alpha = 0.6f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 22.sp
            ),
            color = if (isSelected) colors.colorNavy else colors.colorNavy.copy(alpha = 0.72f)
        )

        if (isSelected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    lineHeight = 20.sp
                ),
                color = colors.colorOrangeMain
            )
        } else {
            Spacer(Modifier.width(16.dp))
        }
    }
}
