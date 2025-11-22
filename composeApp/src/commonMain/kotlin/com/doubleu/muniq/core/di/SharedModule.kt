package com.doubleu.muniq.core.di

import com.doubleu.muniq.presentation.home.HomeViewModel
import org.koin.dsl.module

val sharedModule = module {
    single { "Hello from Shared ViewModel" }
    factory { HomeViewModel(get()) }
}
