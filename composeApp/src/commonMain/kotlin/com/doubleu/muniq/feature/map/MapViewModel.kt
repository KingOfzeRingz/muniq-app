package com.doubleu.muniq.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubleu.muniq.app.di.ServiceLocator
import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.domain.ScoreCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flow

class MapViewModel : ViewModel() {
    private val districtRepository = ServiceLocator.districtRepository
    private val userPreferencesRepository = ServiceLocator.userPreferencesRepository

    private val _rawDistricts = flow {
        emit(districtRepository.getAllDistricts())
    }

    val districts: StateFlow<List<District>> = combine(
        _rawDistricts,
        userPreferencesRepository.importantMetrics
    ) { districts, importantMetrics ->
        ScoreCalculator.calculateScores(districts, importantMetrics)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}