package com.doubleu.muniq.feature.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubleu.muniq.app.di.ServiceLocator
import com.doubleu.muniq.core.model.MetricType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PriorityUiState(
    val important: List<MetricType> = emptyList(),
    val notRelevant: List<MetricType> = emptyList()
)

class PriorityViewModel : ViewModel() {
    private val repository = ServiceLocator.userPreferencesRepository

    val uiState: StateFlow<PriorityUiState> = combine(
        repository.importantMetrics,
        repository.notRelevantMetrics
    ) { important, notRelevant ->
        PriorityUiState(important, notRelevant)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PriorityUiState()
    )

    fun moveToImportant(type: MetricType) {
        repository.moveToImportant(type)
    }

    fun moveToNotRelevant(type: MetricType) {
        repository.moveToNotRelevant(type)
    }
}