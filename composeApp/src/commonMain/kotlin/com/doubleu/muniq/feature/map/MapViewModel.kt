package com.doubleu.muniq.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubleu.muniq.app.di.ServiceLocator
import com.doubleu.muniq.core.model.District
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flow

class MapViewModel : ViewModel() {
    private val districtRepository = ServiceLocator.districtRepository

    // Simply fetch districts - scoring happens in MunichMapContent based on user preferences
    val districts: StateFlow<List<District>> = flow {
        emit(districtRepository.getAllDistricts())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )
}