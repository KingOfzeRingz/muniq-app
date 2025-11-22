package com.doubleu.muniq.app.di

import com.doubleu.muniq.data.DistrictRepository
import com.doubleu.muniq.data.FakeDistrictRepository

object ServiceLocator {

    val districtRepository: DistrictRepository by lazy {
        FakeDistrictRepository()
    }
}