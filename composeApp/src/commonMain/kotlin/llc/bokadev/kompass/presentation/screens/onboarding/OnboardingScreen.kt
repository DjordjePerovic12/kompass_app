package llc.bokadev.kompass.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorDustySage
import llc.bokadev.kompass.presentation.theme.colorRoseClay
import llc.bokadev.kompass.presentation.theme.helveticaBold35
import llc.bokadev.kompass.presentation.theme.helveticaRegular13

private enum class OnboardingContext(val storageValue: String) {
    PlanningAhead("planning_ahead"),
    AlreadyHere("already_here")
}

@Composable
fun OnboardingScreen(
    initialContext: String = OnboardingContext.PlanningAhead.storageValue,
    onComplete: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var selectedContext by remember {
        mutableStateOf(
            when (initialContext) {
                OnboardingContext.AlreadyHere.storageValue -> OnboardingContext.AlreadyHere
                else -> OnboardingContext.PlanningAhead
            }
        )
    }

    fun finishOnboarding() {
        onComplete(selectedContext.storageValue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorHomeCanvas)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ðir by KOMPASS",
                    style = helveticaRegular13(),
                    color = colors.colorNavy.copy(alpha = 0.56f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 20.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) colors.colorOrangeMain
                                    else colors.colorSurfaceMid.copy(alpha = 0.72f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> ContextFramingPage(
                        selectedContext = selectedContext
                    )

                    1 -> AtmospherePage(selectedContext = selectedContext)
                    else -> OpenAccessPage()
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    Text(
                        text = "Back",
                        modifier = Modifier.clickable {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        style = helveticaRegular13(),
                        color = colors.colorNavy.copy(alpha = 0.62f)
                    )
                } else {
                    Spacer(Modifier.width(36.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.colorOrangeMain)
                        .clickable {
                            if (pagerState.currentPage == 2) {
                                finishOnboarding()
                            } else {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == 2) "Enter Kotor" else "Continue",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.colorWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextFramingPage(
    selectedContext: OnboardingContext
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EditorialHeroPanel(
            eyebrow = "Entry framing",
            title = "Kotor reveals itself gradually.",
            body = "Move through the bay slowly, or let it meet you in real time. KOMPASS begins by understanding the rhythm you are in."
        )

        ContextChoiceCard(
            title = "Planning Ahead",
            body = "Explore Kotor slowly, save places, and understand the rhythm of the bay before arriving.",
            accent = colorDustySage,
            selected = selectedContext == OnboardingContext.PlanningAhead,
            onClick = null
        )

        ContextChoiceCard(
            title = "Already Here",
            body = "Discover what’s around you, continue beyond the obvious, and experience Kotor more deeply in real time.",
            accent = colorRoseClay,
            selected = selectedContext == OnboardingContext.AlreadyHere,
            onClick = null
        )
    }
}

@Composable
private fun AtmospherePage(
    selectedContext: OnboardingContext
) {
    val emphasis = if (selectedContext == OnboardingContext.AlreadyHere) {
        "Nearby places, quieter continuations, layered stories, and spatial continuity shape what comes next."
    } else {
        "Curated places, saved rhythms, layered stories, and a broader sense of geography shape what waits ahead."
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EditorialHeroPanel(
            eyebrow = "Orientation",
            title = "Move beyond the obvious.",
            body = "KOMPASS is less interested in ticking off landmarks than in helping the destination unfold with more continuity, atmosphere, and depth."
        )

        QuietStatementCard(
            lines = listOf(
                "Quieter paths and continuations beyond crowded areas",
                "Short contextual moments instead of constant narration",
                emphasis
            )
        )
    }
}

@Composable
private fun OpenAccessPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EditorialHeroPanel(
            eyebrow = "Open access",
            title = "Explore freely.",
            body = "Everything in KOMPASS remains fully explorable without friction or artificial restrictions. Discovery stays open, calm, and generous from the start."
        )

        QuietStatementCard(
            lines = listOf(
                "Discovery, maps, routes, and planning remain open to everyone.",
                "Use the app to move through Kotor with more orientation, continuity, and local context.",
                "A clear destination experience matters more than feature overload."
            )
        )
    }
}

@Composable
private fun EditorialHeroPanel(
    eyebrow: String,
    title: String,
    body: String
) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.colorOrangeMain.copy(alpha = 0.16f),
                        colors.colorWhite
                    )
                )
            )
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.75f), RoundedCornerShape(32.dp))
            .padding(horizontal = 22.dp, vertical = 26.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = eyebrow.uppercase(),
                style = helveticaRegular13(),
                color = colors.colorNavy.copy(alpha = 0.52f),
                letterSpacing = 2.sp
            )
            Text(
                text = title,
                style = helveticaBold35(),
                color = colors.colorNavy,
                lineHeight = 40.sp
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = colors.colorNavy.copy(alpha = 0.74f)
            )
        }
    }
}

@Composable
private fun ContextChoiceCard(
    title: String,
    body: String,
    accent: Color,
    selected: Boolean,
    onClick: (() -> Unit)?
) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) accent.copy(alpha = 0.18f) else colors.colorWhite)
            .border(
                1.dp,
                if (selected) accent.copy(alpha = 0.42f) else colors.colorSurfaceMid.copy(alpha = 0.72f),
                RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.4).sp
                ),
                color = colors.colorNavy
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = colors.colorNavy.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun QuietStatementCard(
    lines: List<String>
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.colorWhite)
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.72f), RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = colors.colorNavy.copy(alpha = 0.76f),
                textAlign = TextAlign.Start
            )
        }
    }
}
