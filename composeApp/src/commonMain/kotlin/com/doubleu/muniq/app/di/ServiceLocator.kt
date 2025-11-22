package com.doubleu.muniq.app.di

import com.doubleu.muniq.data.DistrictRepository
import com.doubleu.muniq.data.DistrictRepositoryImpl
import com.doubleu.muniq.data.UserPreferencesRepository

object ServiceLocator {

    val districtRepository: DistrictRepository by lazy {
        DistrictRepositoryImpl()
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository()
    }
}