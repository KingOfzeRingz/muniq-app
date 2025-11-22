package com.doubleu.muniq.data

import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.core.model.Metric
import com.doubleu.muniq.core.model.MetricCategory
import com.doubleu.muniq.data.model.DistrictDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DistrictRepositoryImpl : DistrictRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private val apiUrl = "https://my-backend-api-krlvdyxnda-uc.a.run.app/api/green-ratings"

    override suspend fun getAllDistricts(): List<District> {
        return try {
            val dtos: List<DistrictDto> = client.get(apiUrl).body()
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getDistrict(id: String): District? {
        return getAllDistricts().find { it.id == id }
    }
}

private fun DistrictDto.toDomain(): District {
    val metrics = listOf(
        Metric("rent", "Rent affordability", MetricCategory.URBAN_RENT, scores.rent, "${raw.rent_m2} €/m²"),
        Metric("green", "Green Index", MetricCategory.ENVIRONMENT, scores.green, "${raw.green_area_percent}%"),
        Metric("family", "Child friendly", MetricCategory.FAMILY, scores.family, "${raw.childcare_spots} spots"),
        Metric("mobility", "Public Transport", MetricCategory.MOBILITY, scores.mobility, "${raw.pt_stops} stops"),
        Metric("quiet", "Quietness", MetricCategory.ENVIRONMENT, scores.quiet, "${raw.noise_index} dB"),
        Metric("air", "Air Quality", MetricCategory.ENVIRONMENT, scores.air, "${raw.air_pm25} µg/m³"),
        Metric("bike", "Bike friendly", MetricCategory.MOBILITY, scores.bike, "${raw.bike_lanes_km} km"),
        Metric("density", "Population Density", MetricCategory.URBAN_RENT, scores.density, "${raw.population_density} /km²")
    )
    return District(
        id = id.toString(),
        name = name,
        overallScore = scores.overall,
        metrics = metrics
    )
}
