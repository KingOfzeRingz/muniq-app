package com.doubleu.muniq.core.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

fun initKoin() = startKoin {
    modules(sharedModule)
}
