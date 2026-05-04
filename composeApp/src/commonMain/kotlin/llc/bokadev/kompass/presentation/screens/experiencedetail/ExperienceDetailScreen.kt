package llc.bokadev.kompass.presentation.screens.experiencedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExperienceDetailScreen(
    id: String,
    onBack: () -> Unit = {},
    onLearnMore: () -> Unit = {},
    onOpenGuide: (String) -> Unit = {}
) {
    val vm: ExperienceDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Activity detail",
                title = "Activities",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        ExperienceDetailScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onLearnMore = onLearnMore,
            onOpenGuide = onOpenGuide
        )
    }
}

@Composable
private fun ExperienceDetailScreenContent(
    state: ExperienceDetailState,
    onIntent: (ExperienceDetailEvent) -> Unit,
    onLearnMore: () -> Unit,
    onOpenGuide: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()

    when {
        state.error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error, color = colors.colorError)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorAmberDark,
                    modifier = Modifier.clickable { onIntent(ExperienceDetailEvent.Retry) }
                )
            }
        }

        state.activity != null -> {
            val activity = state.activity
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(colors.colorSurface)
            ) {
                PlacePhotoHeader(
                    imageUrl = activity.photos.firstOrNull()?.let { buildPhotoUrl(it) }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.colorWhite, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = activity.localizedName(lang),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.colorNavy
                    )

                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        activity.category?.takeIf(String::isNotBlank)?.let { InfoChip(it.prettyActivityCategory()) }
                        activity.durationMin?.let { InfoChip("$it min") }
                        InfoChip(activity.bestTime.toUiLabel())
                    }

                    DetailSection("Overview", activity.localizedDescription(lang))

                    activity.localizedLocation(lang).takeIf(String::isNotBlank)?.let {
                        DetailSection("Location", it)
                    }

                    activity.localizedHowToGetThere(lang).takeIf(String::isNotBlank)?.let {
                        DetailSection("How to get there", it)
                    }

                    if (activity.audioFile != null) {
                        PremiumSection(
                            title = "Audio Guide",
                            body = if (state.hasAudioAccess) {
                                "Audio guide is unlocked for this activity. Open the premium guide screen for map context and native background playback."
                            } else {
                                "This audio story is part of the Audio Pass and can be unlocked once premium purchases are wired in."
                            },
                            isLocked = !state.hasAudioAccess,
                            showLearnMore = !state.hasAudioAccess,
                            onLearnMore = onLearnMore,
                            ctaLabel = if (state.hasAudioAccess) "OPEN GUIDE" else "LEARN MORE",
                            onPrimaryAction = {
                                if (state.hasAudioAccess) {
                                    onOpenGuide(activity.id)
                                } else {
                                    onLearnMore()
                                }
                            }
                        )
                    }

                    activity.localizedLongDescription(lang).takeIf(String::isNotBlank)?.let { longText ->
                        PremiumSection(
                            title = "Deep Dive",
                            body = if (state.hasDetailAccess) longText else "This deeper guide, route context, and local insight are part of Explorer Pass content.",
                            isLocked = !state.hasDetailAccess
                        )
                    }

                    activity.externalWebsite?.takeIf(String::isNotBlank)?.let {
                        DetailSection("External link", it)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    body: String
) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.colorSlate
        )
    }
}

@Composable
private fun PremiumSection(
    title: String,
    body: String,
    isLocked: Boolean,
    showLearnMore: Boolean = false,
    onLearnMore: () -> Unit = {},
    ctaLabel: String = "LEARN MORE",
    onPrimaryAction: (() -> Unit)? = null
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isLocked) colors.colorAmberSubtle else colors.colorSurface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isLocked) "$title · Premium" else title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.colorSlate
        )
        if (showLearnMore || onPrimaryAction != null) {
            Button(
                onClick = onPrimaryAction ?: onLearnMore,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(ctaLabel)
            }
        }
    }
}

private fun String.prettyActivityCategory(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

private fun llc.bokadev.kompass.domain.model.BestTime.toUiLabel(): String = when (this) {
    llc.bokadev.kompass.domain.model.BestTime.MORNING -> "Morning"
    llc.bokadev.kompass.domain.model.BestTime.AFTERNOON -> "Afternoon"
    llc.bokadev.kompass.domain.model.BestTime.EVENING -> "Evening"
    llc.bokadev.kompass.domain.model.BestTime.ANYTIME -> "Any time"
}
