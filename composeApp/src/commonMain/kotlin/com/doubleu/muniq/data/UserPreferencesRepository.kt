package com.doubleu.muniq.data

import com.doubleu.muniq.core.model.MetricType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WeightedMetric(
    val type: MetricType,
    val weight: Float // Position-based weight: first item = highest weight
)

class UserPreferencesRepository {
    private val _importantMetrics = MutableStateFlow(
        listOf(
            MetricType.RENT,
            MetricType.GREEN,
            MetricType.CHILD,
            MetricType.STUDENT
        )
    )
    val importantMetrics: StateFlow<List<MetricType>> = _importantMetrics.asStateFlow()

    private val _notRelevantMetrics = MutableStateFlow(
        listOf(
            MetricType.QUIET,
            MetricType.AIR,
            MetricType.BIKE,
            MetricType.DENSITY
        )
    )
    val notRelevantMetrics: StateFlow<List<MetricType>> = _notRelevantMetrics.asStateFlow()

    /**
     * Returns weighted metrics based on position in the important list.
     * First item gets weight 1.0, and each subsequent item gets progressively less weight.
     * Weight calculation: weight = 1.0 - (position * 0.15), minimum 0.1
     */
    fun getWeightedMetrics(): List<WeightedMetric> {
        return _importantMetrics.value.mapIndexed { index, type ->
            val weight = maxOf(0.1f, 1.0f - (index * 0.15f))
            WeightedMetric(type, weight)
        }
    }

    fun moveToImportant(type: MetricType) {
        _importantMetrics.update { it + type }
        _notRelevantMetrics.update { it - type }
    }

    fun moveToNotRelevant(type: MetricType) {
        _importantMetrics.update { it - type }
        _notRelevantMetrics.update { it + type }
    }

    fun reorderImportant(newOrder: List<MetricType>) {
        _importantMetrics.update { newOrder }
    }
}
