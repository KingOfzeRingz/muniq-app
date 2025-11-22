package com.doubleu.muniq.data

import com.doubleu.muniq.core.model.MetricType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun moveToImportant(type: MetricType) {
        _importantMetrics.update { it + type }
        _notRelevantMetrics.update { it - type }
    }

    fun moveToNotRelevant(type: MetricType) {
        _importantMetrics.update { it - type }
        _notRelevantMetrics.update { it + type }
    }
}
