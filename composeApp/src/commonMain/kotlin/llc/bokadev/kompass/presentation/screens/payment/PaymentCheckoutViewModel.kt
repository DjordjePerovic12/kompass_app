package llc.bokadev.kompass.presentation.screens.payment

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.data.repository.PaymentCheckoutSessionStore
import llc.bokadev.kompass.domain.model.PaymentCheckoutSession
import llc.bokadev.kompass.domain.model.PaymentVerificationResult
import llc.bokadev.kompass.domain.model.PaymentVerificationStatus
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.VerifyPremiumCheckoutUseCase

data class PaymentCheckoutState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val session: PaymentCheckoutSession? = null,
    val result: PaymentVerificationResult? = null
) : BaseState()

sealed interface PaymentCheckoutEvent : BaseEvent {
    data class PageNavigated(val url: String) : PaymentCheckoutEvent
    data object RetryVerification : PaymentCheckoutEvent
}

class PaymentCheckoutViewModel(
    private val sessionId: String,
    private val sessionStore: PaymentCheckoutSessionStore,
    private val verifyPremiumCheckout: VerifyPremiumCheckoutUseCase,
    private val premiumRepository: PremiumRepository
) : BaseViewModel<PaymentCheckoutState, PaymentCheckoutEvent>() {

    override val initialState = PaymentCheckoutState()

    init {
        val session = sessionStore.get(sessionId)
        _state.update {
            it.copy(
                isLoading = false,
                error = if (session == null) "Checkout session expired. Please try again." else null,
                session = session
            )
        }
    }

    override fun onIntent(event: PaymentCheckoutEvent) {
        when (event) {
            is PaymentCheckoutEvent.PageNavigated -> handleNavigation(event.url)
            PaymentCheckoutEvent.RetryVerification -> verify()
        }
    }

    private fun handleNavigation(url: String) {
        val session = state.value.session ?: return
        if (state.value.result != null || state.value.isLoading) return

        when {
            url.startsWith(session.cancelUrl) -> {
                sessionStore.clear(session.sessionId)
                _state.update {
                    it.copy(
                        result = PaymentVerificationResult(
                            status = PaymentVerificationStatus.CANCELED,
                            message = "Checkout was canceled."
                        )
                    )
                }
            }

            url.startsWith(session.successUrl) || url.startsWith(session.errorUrl) -> verify()
        }
    }

    private fun verify() {
        val session = state.value.session ?: return
        if (state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            verifyPremiumCheckout(session.sessionId)
                .onSuccess { result ->
                    if (result.status == PaymentVerificationStatus.SUCCEEDED) {
                        premiumRepository.applyEntitlements(result.entitlements)
                    }
                    if (result.status != PaymentVerificationStatus.PENDING) {
                        sessionStore.clear(session.sessionId)
                    }
                    _state.update { it.copy(isLoading = false, result = result) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "We couldn't verify this checkout yet."
                        )
                    }
                }
        }
    }
}
