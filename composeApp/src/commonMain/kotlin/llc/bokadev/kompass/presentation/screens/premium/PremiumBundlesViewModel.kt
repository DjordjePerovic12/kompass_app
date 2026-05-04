package llc.bokadev.kompass.presentation.screens.premium

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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

    init {
        ensureDemoAudioPass()
    }

    override fun onIntent(event: PremiumBundlesEvent) {
        when (event) {
            PremiumBundlesEvent.CheckoutNavigationHandled -> {
                _state.update { it.copy(pendingCheckoutSession = null) }
            }
            PremiumBundlesEvent.RefreshEntitlements -> {
                ensureDemoAudioPass()
                _state.update { it.copy(entitlements = premiumRepository.getEntitlements()) }
            }
            is PremiumBundlesEvent.StartCheckout -> startCheckout(
                productId = event.productId,
                locale = event.locale
            )
        }
    }

    private fun unlockLocally(productId: String) {
        val product = PremiumCatalog.find(productId)
            ?: run {
                _state.update { it.copy(error = "Unknown premium product.") }
                return
            }

        val current = premiumRepository.getEntitlements()
        val updated = when (product.tier) {
            "audio_pass" -> current.copy(audioPass = true)
            "explorer_pass" -> current.copy(audioPass = true, explorerPass = true)
            "perks_pass" -> current.copy(audioPass = true, explorerPass = true, perksPass = true)
            else -> current
        }
        premiumRepository.applyEntitlements(updated)
        _state.update {
            it.copy(
                entitlements = updated,
                error = null,
                activeCheckoutProductId = null,
                pendingCheckoutSession = null
            )
        }
    }

    private fun startCheckout(productId: String, locale: String) {
        val product = PremiumCatalog.find(productId)
            ?: run {
                _state.update { it.copy(error = "Unknown premium product.") }
                return
            }

        if (product.tier == "audio_pass") {
            unlockLocally(productId)
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

            delay(1400)
            unlockLocally(productId)
            _state.update {
                it.copy(
                    isLoading = false,
                    activeCheckoutProductId = null
                )
            }
        }
    }

    private fun ensureDemoAudioPass() {
        val current = premiumRepository.getEntitlements()
        if (current.audioPass) return
        val updated = current.copy(audioPass = true)
        premiumRepository.applyEntitlements(updated)
    }
}
