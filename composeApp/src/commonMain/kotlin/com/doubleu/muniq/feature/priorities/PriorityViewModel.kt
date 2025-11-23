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

class PriorityViewModel(
    private val repository: com.doubleu.muniq.data.UserPreferencesRepository
) : ViewModel() {

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

    fun reorderImportant(newOrder: List<MetricType>) {
        repository.reorderImportant(newOrder)
    }

    fun moveUp(metric: MetricType) {
        val currentList = uiState.value.important.toMutableList()
        val index = currentList.indexOf(metric)
        if (index > 0) {
            currentList.removeAt(index)
            currentList.add(index - 1, metric)
            reorderImportant(currentList)
        }
    }

    fun moveDown(metric: MetricType) {
        val currentList = uiState.value.important.toMutableList()
        val index = currentList.indexOf(metric)
        if (index < currentList.size - 1) {
            currentList.removeAt(index)
            currentList.add(index + 1, metric)
            reorderImportant(currentList)
        }
    }
}