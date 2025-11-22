package com.doubleu.muniq.data

import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.core.model.Metric
import com.doubleu.muniq.core.model.MetricCategory

class FakeDistrictRepository : DistrictRepository {

    private val districts: List<District> = listOf(
        District(
            id = "schwabing",
            name = "Schwabing-West",
            overallScore = 84,
            metrics = listOf(
                Metric("green", "Green Space", MetricCategory.ENVIRONMENT, 75),
                Metric("quiet", "Quietness", MetricCategory.ENVIRONMENT, 68),
                Metric("pt", "Public Transport", MetricCategory.MOBILITY, 90),
                // ...
            )
        )
    )

    override suspend fun getAllDistricts(): List<District> = districts

    override suspend fun getDistrict(id: String): District? =
        districts.firstOrNull { it.id == id }
}
