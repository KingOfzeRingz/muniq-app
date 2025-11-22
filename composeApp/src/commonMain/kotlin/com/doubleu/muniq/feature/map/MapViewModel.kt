package com.doubleu.muniq.feature.map

import com.doubleu.muniq.app.di.ServiceLocator
import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.data.DistrictRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: DistrictRepository = ServiceLocator.districtRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _districts = MutableStateFlow<List<District>>(emptyList())
    val districts: StateFlow<List<District>> = _districts

    private val _selectedDistrictId = MutableStateFlow<String?>(null)
    val selectedDistrictId: StateFlow<String?> = _selectedDistrictId

    init {
        scope.launch {
            _districts.value = repository.getAllDistricts()
        }
    }

    fun onDistrictTapped(id: String) {
        _selectedDistrictId.value = id
    }
}