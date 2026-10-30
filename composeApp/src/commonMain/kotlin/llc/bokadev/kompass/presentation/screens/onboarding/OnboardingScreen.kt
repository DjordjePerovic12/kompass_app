package llc.bokadev.kompass.presentation.screens.onboarding

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** A brief, skippable first-run introduction (one screen). It only describes what the pilot offers. */
@Composable
fun OnboardingScreen(onAction: (OnboardingAction) -> Unit) {
    val colors = LamplightTheme.colors
    val s = rememberAppStrings().onboarding

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.lamp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(text = "KOMPASS", style = LamplightType.label().copy(fontSize = 14.sp), color = colors.night)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = s.title,
                style = LamplightType.areaTitle(),
                color = colors.night,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = s.body, style = LamplightType.body().copy(fontSize = 17.sp, lineHeight = 25.sp), color = colors.night)
            Spacer(Modifier.padding(top = 4.dp))
            listOf(s.pointLocals, s.pointPractical, s.pointStories).forEach { point ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors.night))
                    Text(text = point, style = LamplightType.body(), color = colors.night)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(LamplightShapes.pill)
                .background(colors.night)
                .clickable(role = Role.Button) { onAction(OnboardingAction.Start) },
            contentAlignment = Alignment.Center
        ) {
            Text(text = s.start, style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold), color = colors.lamp)
        }
    }
}
