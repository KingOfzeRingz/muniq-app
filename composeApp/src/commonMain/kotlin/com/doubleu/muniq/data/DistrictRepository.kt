package com.doubleu.muniq.data

import com.doubleu.muniq.core.model.District

interface DistrictRepository {
    suspend fun getAllDistricts(): List<District>
    suspend fun getDistrict(id: String): District?
}