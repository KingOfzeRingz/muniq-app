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
    val childcare_spots: Double,  // API returns as Double, not Int
    val rent_m2: Double,
    val population_density: Double  // API returns as Double, not Int
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
) {
    fun getScoreFor(type: com.doubleu.muniq.core.model.MetricType): Int {
        return when (type) {
            com.doubleu.muniq.core.model.MetricType.GREEN -> green
            com.doubleu.muniq.core.model.MetricType.QUIET -> quiet
            com.doubleu.muniq.core.model.MetricType.AIR -> air
            com.doubleu.muniq.core.model.MetricType.BIKE -> bike
            com.doubleu.muniq.core.model.MetricType.CHILD -> family
            com.doubleu.muniq.core.model.MetricType.DENSITY -> density
            com.doubleu.muniq.core.model.MetricType.RENT -> rent
            com.doubleu.muniq.core.model.MetricType.STUDENT -> mobility  // Map student to mobility
        }
    }
}
