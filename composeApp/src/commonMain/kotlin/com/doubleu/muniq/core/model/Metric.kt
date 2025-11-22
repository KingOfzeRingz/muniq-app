package com.doubleu.muniq.core.model

data class Metric(
    val id: String,
    val name: String,
    val category: MetricCategory,
    val score: Int,        // 0–100
    val rawValue: String? = null
)

enum class MetricCategory {
    ENVIRONMENT,
    MOBILITY,
    FAMILY,
    URBAN_RENT
}