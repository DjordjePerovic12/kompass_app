package llc.bokadev.kompass.presentation.screens.premium

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.data.repository.PaymentCheckoutSessionStore
import llc.bokadev.kompass.domain.model.PaymentCheckoutSession
import llc.bokadev.kompass.domain.model.PremiumCatalog
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.model.PremiumProduct
import llc.bokadev.kompass.domain.usecase.StartPremiumCheckoutUseCase
import llc.bokadev.kompass.getPlatform

data class PremiumBundlesState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val products: List<PremiumProduct> = PremiumCatalog.products,
    val entitlements: PremiumEntitlements = PremiumEntitlements(),
    val activeCheckoutProductId: String? = null,
    val pendingCheckoutSession: PaymentCheckoutSession? = null
) : BaseState()

sealed interface PremiumBundlesEvent : BaseEvent {
    data class StartCheckout(val productId: String, val locale: String) : PremiumBundlesEvent
    data object CheckoutNavigationHandled : PremiumBundlesEvent
    data object RefreshEntitlements : PremiumBundlesEvent
}

class PremiumBundlesViewModel(
    private val startPremiumCheckout: StartPremiumCheckoutUseCase,
    private val premiumRepository: llc.bokadev.kompass.domain.repository.PremiumRepository,
    private val sessionStore: PaymentCheckoutSessionStore
) : llc.bokadev.kompass.core.presentation.base.BaseViewModel<PremiumBundlesState, PremiumBundlesEvent>() {

    override val initialState = PremiumBundlesState(
        entitlements = premiumRepository.getEntitlements()
    )

    override fun onIntent(event: PremiumBundlesEvent) {
        when (event) {
            PremiumBundlesEvent.CheckoutNavigationHandled -> {
                _state.update { it.copy(pendingCheckoutSession = null) }
            }
            PremiumBundlesEvent.RefreshEntitlements -> {
                _state.update { it.copy(entitlements = premiumRepository.getEntitlements()) }
            }
            is PremiumBundlesEvent.StartCheckout -> startCheckout(
                productId = event.productId,
                locale = event.locale
            )
        }
    }

    private fun startCheckout(productId: String, locale: String) {
        val product = PremiumCatalog.find(productId)
            ?: run {
                _state.update { it.copy(error = "Unknown premium product.") }
                return
            }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    activeCheckoutProductId = productId
                )
            }

            startPremiumCheckout(product, locale, getPlatform().name)
                .onSuccess { session ->
                    sessionStore.put(session)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            activeCheckoutProductId = null,
                            pendingCheckoutSession = session,
                            entitlements = premiumRepository.getEntitlements()
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            activeCheckoutProductId = null,
                            error = error.message ?: "We couldn't start checkout right now."
                        )
                    }
                }
        }
    }
}
