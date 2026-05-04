package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.PaymentCheckoutSessionStore
import llc.bokadev.kompass.data.repository.PaymentRepositoryImpl
import llc.bokadev.kompass.data.repository.PremiumRepositoryImpl
import llc.bokadev.kompass.domain.repository.PaymentRepository
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase
import llc.bokadev.kompass.domain.usecase.StartPremiumCheckoutUseCase
import llc.bokadev.kompass.domain.usecase.VerifyPremiumCheckoutUseCase
import llc.bokadev.kompass.presentation.screens.payment.PaymentCheckoutViewModel
import llc.bokadev.kompass.presentation.screens.premium.PremiumBundlesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val premiumModule = module {
    single { PaymentCheckoutSessionStore() }
    single<PremiumRepository> { PremiumRepositoryImpl(get()) }
    single<PaymentRepository> { PaymentRepositoryImpl(get(named("kompassApi"))) }
    factory { HasPremiumAccessUseCase(get()) }
    factory { StartPremiumCheckoutUseCase(get()) }
    factory { VerifyPremiumCheckoutUseCase(get()) }
    viewModel { PremiumBundlesViewModel(get(), get(), get()) }
    viewModel { (sessionId: String) -> PaymentCheckoutViewModel(sessionId, get(), get(), get()) }
}
