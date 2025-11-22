package com.doubleu.muniq.domain

import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.core.model.MetricType

object ScoreCalculator {
    fun calculateScores(districts: List<District>, importantMetrics: List<MetricType>): List<District> {
        if (importantMetrics.isEmpty()) return districts.map { it.copy(overallScore = 0) }

        return districts.map { district ->
            val totalScore = importantMetrics.sumOf { metricType ->
                getScoreForMetric(district, metricType)
            }
            val averageScore = totalScore / importantMetrics.size
            district.copy(overallScore = averageScore)
        }
    }

    private fun getScoreForMetric(district: District, type: MetricType): Int {
        val metric = district.metrics.find { it.id == type.key }
        return metric?.score ?: 0
    }
}
