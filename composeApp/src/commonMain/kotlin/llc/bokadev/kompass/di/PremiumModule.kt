package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.PaymentCheckoutSessionStore
import llc.bokadev.kompass.data.repository.PaymentRepositoryImpl
import llc.bokadev.kompass.data.repository.PilotDeepPurchaseRepositoryImpl
import llc.bokadev.kompass.data.repository.PremiumRepositoryImpl
import llc.bokadev.kompass.domain.repository.DeepPurchaseRepository
import llc.bokadev.kompass.domain.repository.PaymentRepository
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase
import llc.bokadev.kompass.domain.usecase.StartPremiumCheckoutUseCase
import llc.bokadev.kompass.domain.usecase.VerifyPremiumCheckoutUseCase
import llc.bokadev.kompass.domain.usecase.GetAudioCatalogUseCase
import llc.bokadev.kompass.presentation.screens.listen.ListenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val premiumModule = module {
    single { PaymentCheckoutSessionStore() }
    single<PremiumRepository> { PremiumRepositoryImpl(get()) }
    single<DeepPurchaseRepository> { PilotDeepPurchaseRepositoryImpl(get()) }
    single<PaymentRepository> { PaymentRepositoryImpl(get(named("kompassApi"))) }
    factory { HasPremiumAccessUseCase(get()) }
    factory { StartPremiumCheckoutUseCase(get()) }
    factory { VerifyPremiumCheckoutUseCase(get()) }
    factory { GetAudioCatalogUseCase(get(), get(), get()) }
    viewModelOf(::ListenViewModel)
}
