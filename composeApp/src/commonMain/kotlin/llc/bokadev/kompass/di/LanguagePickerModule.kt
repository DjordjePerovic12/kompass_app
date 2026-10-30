package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerViewModel
import llc.bokadev.kompass.presentation.screens.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val languagePickerModule = module {
    viewModel { LanguagePickerViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
}
