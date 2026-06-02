package llc.bokadev.kompass.presentation.screens.premium

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.PremiumCatalog
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.model.PremiumProduct
import llc.bokadev.kompass.domain.repository.DeepPurchaseRepository
import llc.bokadev.kompass.domain.repository.PremiumRepository

data class PremiumBundlesState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val products: List<PremiumProduct> = PremiumCatalog.products,
    val entitlements: PremiumEntitlements = PremiumEntitlements(),
    val activePurchaseProductId: String? = null,
    val isRestoring: Boolean = false,
    val justUnlockedDeep: Boolean = false
) : BaseState()

sealed interface PremiumBundlesEvent : BaseEvent {
    data class StartCheckout(val productId: String) : PremiumBundlesEvent
    data object RefreshEntitlements : PremiumBundlesEvent
    data object RestorePurchases : PremiumBundlesEvent
    data object DeepNavigationHandled : PremiumBundlesEvent
}

class PremiumBundlesViewModel(
    private val deepPurchaseRepository: DeepPurchaseRepository,
    private val premiumRepository: PremiumRepository
) : BaseViewModel<PremiumBundlesState, PremiumBundlesEvent>() {

    override val initialState = PremiumBundlesState(
        entitlements = premiumRepository.getEntitlements()
    )

    override fun onIntent(event: PremiumBundlesEvent) {
        when (event) {
            PremiumBundlesEvent.DeepNavigationHandled -> {
                _state.update { it.copy(justUnlockedDeep = false) }
            }
            PremiumBundlesEvent.RefreshEntitlements -> refresh()
            PremiumBundlesEvent.RestorePurchases -> restore()
            is PremiumBundlesEvent.StartCheckout -> purchaseDeep(event.productId)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val entitlements = deepPurchaseRepository.syncEntitlements()
            val deepProduct = deepPurchaseRepository.getDeepProduct().getOrNull()

            _state.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    entitlements = entitlements,
                    products = it.products.map { product ->
                        if (product.tier == "audio_pass") {
                            product.copy(
                                title = deepProduct?.title ?: product.title,
                                priceLabel = deepProduct?.priceLabel ?: product.priceLabel
                            )
                        } else {
                            product
                        }
                    }
                )
            }
        }
    }

    private fun purchaseDeep(productId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    activePurchaseProductId = productId
                )
            }

            deepPurchaseRepository.purchaseDeep()
                .onSuccess { entitlements ->
                    val refreshedProduct = deepPurchaseRepository.getDeepProduct().getOrNull()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            activePurchaseProductId = null,
                            entitlements = entitlements,
                            justUnlockedDeep = entitlements.audioPass,
                            products = it.products.map { product ->
                                if (product.tier == "audio_pass") {
                                    product.copy(
                                        title = refreshedProduct?.title ?: product.title,
                                        priceLabel = refreshedProduct?.priceLabel ?: product.priceLabel
                                    )
                                } else {
                                    product
                                }
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            activePurchaseProductId = null,
                            error = error.message ?: "We couldn't start the purchase right now."
                        )
                    }
                }
        }
    }

    private fun restore() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isRestoring = true) }

            deepPurchaseRepository.restoreDeep()
                .onSuccess { entitlements ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            isRestoring = false,
                            entitlements = entitlements,
                            justUnlockedDeep = entitlements.audioPass
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRestoring = false,
                            error = error.message ?: "We couldn't restore purchases right now."
                        )
                    }
                }
        }
    }
}
