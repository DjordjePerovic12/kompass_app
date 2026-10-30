package llc.bokadev.kompass.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.onboarding_body
import kompass.composeapp.generated.resources.onboarding_point_locals
import kompass.composeapp.generated.resources.onboarding_point_practical
import kompass.composeapp.generated.resources.onboarding_point_stories
import kompass.composeapp.generated.resources.onboarding_start
import kompass.composeapp.generated.resources.onboarding_title
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource

/** A brief, skippable first-run introduction (one screen). It only describes what the pilot offers. */
@Composable
fun OnboardingScreenContent(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit
) {
    val colors = KompassTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.lampYellow)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(text = "KOMPASS", style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 14.sp), color = colors.nightBlack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.onboarding_title),
                style = KompassTheme.typography.gloockRegular40,
                color = colors.nightBlack,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(Res.string.onboarding_body),
                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 17.sp, lineHeight = 25.sp),
                color = colors.nightBlack
            )
            Spacer(Modifier.padding(top = 4.dp))
            listOf(
                stringResource(Res.string.onboarding_point_locals),
                stringResource(Res.string.onboarding_point_practical),
                stringResource(Res.string.onboarding_point_stories)
            ).forEach { point ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors.nightBlack))
                    Text(text = point, style = KompassTheme.typography.geologicaMedium16, color = colors.nightBlack)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(KompassShapes.pill)
                .background(colors.nightBlack)
                .clickable(role = Role.Button, onClick = onStartClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.onboarding_start),
                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
                color = colors.lampYellow
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenContentPreview() {
    KompassTheme {
        OnboardingScreenContent(onStartClick = {})
    }
}
