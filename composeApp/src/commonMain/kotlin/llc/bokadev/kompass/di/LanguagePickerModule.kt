package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val languagePickerModule = module {
    viewModel { LanguagePickerViewModel(get()) }
}
