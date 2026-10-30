package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.AudioGuideRepositoryImpl
import llc.bokadev.kompass.data.repository.ExperienceRepositoryImpl
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.domain.repository.ExperienceRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import llc.bokadev.kompass.domain.usecase.GetActivityByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetSignedAudioUrlUseCase
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceDetailViewModel
import org.koin.core.qualifier.named
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val experiencesModule = module {
    single<ExperienceRepository> { ExperienceRepositoryImpl(get(), get()) }
    single<AudioGuideRepository> { AudioGuideRepositoryImpl(get(), get(named("kompassApi")), get()) }
    factory { GetActivitiesUseCase(get()) }
    factory { GetActivityByIdUseCase(get()) }
    factory { GetSignedAudioUrlUseCase(get()) }
    viewModel { (id: String) -> ExperienceDetailViewModel(id, get(), get(), get(), get(), get(), get(), get()) }
}
