package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.languagepicker.LanguagePickerViewModel
import llc.bokadev.kompass.presentation.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val languagePickerModule = module {
    viewModelOf(::LanguagePickerViewModel)
    viewModelOf(::OnboardingViewModel)
}
