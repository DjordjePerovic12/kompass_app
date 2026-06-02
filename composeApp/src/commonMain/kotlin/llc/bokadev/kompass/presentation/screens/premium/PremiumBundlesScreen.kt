package llc.bokadev.kompass.presentation.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.domain.model.PremiumProduct
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PremiumBundlesScreen(
    onBack: () -> Unit,
    unlockTargetActivityId: String? = null,
    onNavigateToGuide: (String) -> Unit = {}
) {
    val vm: PremiumBundlesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val colors = KompassTheme.colors
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        vm.onIntent(PremiumBundlesEvent.RefreshEntitlements)
        analytics.trackScreenView("premium_bundles")
        analytics.trackPremiumBundleOpen(contentOrigin = if (unlockTargetActivityId != null) "activity_upsell" else "premium")
    }

    LaunchedEffect(state.justUnlockedDeep) {
        if (state.justUnlockedDeep && unlockTargetActivityId != null) {
            onNavigateToGuide(unlockTargetActivityId)
            vm.onIntent(PremiumBundlesEvent.DeepNavigationHandled)
        }
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "A quieter companion layer through Kotor",
                title = "KOMPASS Deep",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.colorSurface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (unlockTargetActivityId != null && state.entitlements.audioPass) {
                Button(
                    onClick = { onNavigateToGuide(unlockTargetActivityId) },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("OPEN DEEP GUIDE")
                }
            }

            state.products.forEach { product ->
                DeepIntroCard(
                    product = product,
                    isUnlocked = state.entitlements.hasAccess(product.tier),
                    isProcessing = state.activePurchaseProductId == product.id,
                    onUnlock = {
                        vm.onIntent(
                            PremiumBundlesEvent.StartCheckout(
                                productId = product.id
                            )
                        )
                    }
                )
            }

            TextButton(
                onClick = { vm.onIntent(PremiumBundlesEvent.RestorePurchases) },
                enabled = !state.isRestoring && !state.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (state.isRestoring) "RESTORING..." else "Restore purchases")
            }

            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorError
                )
            }
        }
    }
}

@Composable
private fun DeepIntroCard(
    product: PremiumProduct,
    isUnlocked: Boolean,
    isProcessing: Boolean,
    onUnlock: () -> Unit
) {
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite, RoundedCornerShape(26.dp))
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kotor reveals itself gradually.",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 34.sp
            ),
            color = colors.colorNavy
        )
        Text(
            text = "Beyond landmarks, routes, and viewpoints, KOMPASS Deep adds a quieter companion layer throughout the destination — helping places feel more connected, atmospheric, and alive as you explore.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
            color = colors.colorSlate.copy(alpha = 0.84f)
        )
        Text(
            text = "Instead of long guides or constant narration, Deep offers short contextual moments across selected walks, viewpoints, villages, and experiences throughout the bay.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
            color = colors.colorSlate.copy(alpha = 0.84f)
        )
        listOf(
            "subtle audio companionship during walks",
            "layered local and historical context",
            "quieter continuations beyond crowded areas",
            "viewpoint and atmosphere guidance",
            "spatial stories tied to the landscape around you",
            "suggestions that help the destination unfold more naturally"
        ).forEach { bullet ->
            Text(
                text = "• $bullet",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = colors.colorNavy.copy(alpha = 0.84f)
            )
        }
        Text(
            text = "Deep is designed to enhance the feeling of being in Kotor — not distract from it.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
            color = colors.colorSlate.copy(alpha = 0.84f)
        )
        Text(
            text = "Everything in KOMPASS remains fully explorable without Deep. This layer simply offers a more guided and immersive way to experience the destination for those who want it.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
            color = colors.colorSlate.copy(alpha = 0.84f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.colorHomeCanvas, RoundedCornerShape(22.dp))
                .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "One-time access — ${product.priceLabel}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorNavy
                )
                Text(
                    text = "Valid across all Deep-supported experiences in Kotor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate.copy(alpha = 0.8f)
                )
            }
        }
        Button(
            onClick = onUnlock,
            enabled = !isUnlocked && !isProcessing,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text(
                when {
                    isUnlocked -> "UNLOCKED"
                    isProcessing -> "STARTING PURCHASE..."
                    else -> "GET DEEP — ${product.priceLabel}"
                }
            )
        }
    }
}
