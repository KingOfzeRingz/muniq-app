package com.doubleu.muniq.domain

import com.doubleu.muniq.core.model.MetricType
import com.doubleu.muniq.data.model.DistrictScoresDto

object ScoreCalculator {
    
    /**
     * Calculates a dynamic score (0-100) based on position in priority list.
     * 
     * Position-based weights:
     * - 1st place: 5.0x (dominant)
     * - 2nd place: 3.0x (strong)
     * - 3rd place: 1.5x (moderate)
     * - 4th+ place: 1.0x (normal)
     * - Ignored: 0.0x (excluded)
     * 
     * @param scores The raw scores from the district data
     * @param importantMetrics Ordered list of important metrics (position matters!)
     * @param ignoredMetrics Metrics to completely exclude
     * @return Personalized score from 0-100
     */
    fun calculatePersonalizedScore(
        scores: DistrictScoresDto,
        importantMetrics: List<MetricType>,
        ignoredMetrics: List<MetricType>
    ): Int {
        // Position-based weights for important metrics
        val positionWeights = mapOf(
            0 to 5.0,  // 1st position
            1 to 3.0,  // 2nd position
            2 to 1.5,  // 3rd position
        )
        val defaultWeight = 1.0  // 4th+ positions and standard metrics
        
        // Identify "Standard" metrics (all possible metrics minus important & ignored)
        val allMetrics = MetricType.values().toList()
        val standardMetrics = allMetrics - importantMetrics.toSet() - ignoredMetrics.toSet()

        var totalWeightedScore = 0.0
        var totalMaxPossibleWeight = 0.0

        // Sum up Important Metrics with position-based weights
        importantMetrics.forEachIndexed { index, type ->
            val weight = positionWeights[index] ?: defaultWeight
            totalWeightedScore += scores.getScoreFor(type) * weight
            totalMaxPossibleWeight += 100 * weight
        }

        // Sum up Standard Metrics with default weight
        standardMetrics.forEach { type ->
            totalWeightedScore += scores.getScoreFor(type) * defaultWeight
            totalMaxPossibleWeight += 100 * defaultWeight
        }

        // Calculate Percentage
        if (totalMaxPossibleWeight == 0.0) return 50 // Fallback if everything is ignored

        val finalScore = (totalWeightedScore / totalMaxPossibleWeight) * 100
        return finalScore.toInt().coerceIn(0, 100)
    }
}
