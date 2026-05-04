package llc.bokadev.kompass.presentation.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import llc.bokadev.kompass.domain.model.PaymentVerificationStatus
import llc.bokadev.kompass.domain.model.PremiumCatalog
import llc.bokadev.kompass.presentation.shared.HostedPaymentWebView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PaymentCheckoutScreen(
    sessionId: String,
    onBack: () -> Unit
) {
    val vm: PaymentCheckoutViewModel = koinViewModel(parameters = { parametersOf(sessionId) })
    val state by vm.state.collectAsState()
    val title = state.session?.productId?.let { PremiumCatalog.find(it)?.title } ?: "Checkout"

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Secure hosted payment",
                title = title,
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        val session = state.session
        when {
            session != null && state.result == null -> {
                HostedPaymentWebView(
                    url = session.checkoutUrl,
                    modifier = Modifier.fillMaxSize(),
                    onUrlChange = { vm.onIntent(PaymentCheckoutEvent.PageNavigated(it)) }
                )
            }

            else -> {
                PaymentCheckoutResultContent(
                    state = state,
                    onRetry = { vm.onIntent(PaymentCheckoutEvent.RetryVerification) },
                    onDone = onBack
                )
            }
        }
    }
}

@Composable
private fun PaymentCheckoutResultContent(
    state: PaymentCheckoutState,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    val colors = KompassTheme.colors
    val result = state.result

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.colorWhite, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = when (result?.status) {
                    PaymentVerificationStatus.SUCCEEDED -> "Purchase unlocked"
                    PaymentVerificationStatus.PENDING -> "Payment still processing"
                    PaymentVerificationStatus.CANCELED -> "Checkout canceled"
                    PaymentVerificationStatus.FAILED -> "Payment not completed"
                    null -> "Checkout unavailable"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )

            Text(
                text = state.error
                    ?: result?.message
                    ?: when (result?.status) {
                        PaymentVerificationStatus.SUCCEEDED -> "Your premium access is now available in the app."
                        PaymentVerificationStatus.PENDING -> "The bank flow completed, but the final confirmation has not reached us yet. You can retry verification in a moment."
                        PaymentVerificationStatus.CANCELED -> "No charge was completed."
                        PaymentVerificationStatus.FAILED -> "Please try again or use a different payment method."
                        null -> "We couldn't restore the hosted checkout session."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.colorSlate
            )
        }

        if (result?.status == PaymentVerificationStatus.PENDING || state.error != null) {
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text("RETRY VERIFICATION")
            }
        }

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text("DONE")
        }
    }
}
