package llc.bokadev.kompass.presentation.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.PremiumProduct
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PremiumBundlesScreen(
    onBack: () -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    unlockTargetActivityId: String? = null,
    onNavigateToGuide: (String) -> Unit = {}
) {
    val vm: PremiumBundlesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val locale = currentAppLanguage()
    val strings = rememberAppStrings()
    val colors = KompassTheme.colors
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        vm.onIntent(PremiumBundlesEvent.RefreshEntitlements)
        analytics.trackScreenView("premium_bundles")
        analytics.trackPremiumBundleOpen(contentOrigin = if (unlockTargetActivityId != null) "activity_upsell" else "premium")
    }

    LaunchedEffect(state.pendingCheckoutSession?.sessionId) {
        state.pendingCheckoutSession?.let { session ->
            onNavigateToCheckout(session.sessionId)
            vm.onIntent(PremiumBundlesEvent.CheckoutNavigationHandled)
        }
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = strings.premiumSlug,
                title = strings.premiumTitle,
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
                    Text(strings.openAudioGuideNow)
                }
            }

            Text(
                text = strings.premiumIntro,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.colorSlate
            )

            state.products.forEach { product ->
                BundleCard(
                    product = product,
                    isUnlocked = state.entitlements.hasAccess(product.tier),
                    isProcessing = state.activeCheckoutProductId == product.id,
                    strings = strings,
                    onUnlock = {
                        vm.onIntent(
                            PremiumBundlesEvent.StartCheckout(
                                productId = product.id,
                                locale = locale
                            )
                        )
                        if (product.tier == "audio_pass") {
                            unlockTargetActivityId?.let(onNavigateToGuide)
                        }
                    }
                )
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
private fun BundleCard(
    product: PremiumProduct,
    isUnlocked: Boolean,
    isProcessing: Boolean,
    strings: llc.bokadev.kompass.core.util.AppStrings,
    onUnlock: () -> Unit
) {
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${product.title} · ${product.priceLabel}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = product.description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.colorSlate
        )
        product.features.forEach { bullet ->
            Text(
                text = "- $bullet",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.colorNavy
            )
        }
        Button(
            onClick = onUnlock,
            enabled = !isUnlocked && !isProcessing,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                when {
                    isUnlocked -> strings.unlocked
                    isProcessing -> strings.processingCheckout
                    else -> "${strings.unlockPricePrefix} ${product.priceLabel}"
                }
            )
        }
    }
}
