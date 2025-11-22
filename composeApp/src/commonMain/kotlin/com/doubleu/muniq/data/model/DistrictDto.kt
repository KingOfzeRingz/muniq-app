package com.doubleu.muniq.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DistrictDto(
    val id: Int,
    val name: String,
    val raw: DistrictMetricsDto,
    val scores: DistrictScoresDto,
    val video_url: String? = null
)

@Serializable
data class DistrictMetricsDto(
    val green_area_percent: Double,
    val noise_index: Double,
    val air_pm25: Double,
    val pt_stops: Double,
    val bike_lanes_km: Double,
    val childcare_spots: Int,
    val rent_m2: Double,
    val population_density: Int
)

@Serializable
data class DistrictScoresDto(
    val green: Int,
    val quiet: Int,
    val air: Int,
    val mobility: Int,
    val bike: Int,
    val family: Int,
    val density: Int,
    val rent: Int,
    val overall: Int
)
