package com.doubleu.muniq.core.di

import com.doubleu.muniq.presentation.home.HomeViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val sharedModule = module {
    single { "Hello from Shared ViewModel" }
    
    // Repositories
    single { com.doubleu.muniq.data.UserPreferencesRepository() }
    single<com.doubleu.muniq.data.DistrictRepository> { com.doubleu.muniq.data.DistrictRepositoryImpl() }
    
    // ViewModels
    viewModel { HomeViewModel(get()) }
    viewModel { com.doubleu.muniq.feature.priorities.PriorityViewModel(get()) }
    viewModel { com.doubleu.muniq.feature.map.MapViewModel(get()) }
}
