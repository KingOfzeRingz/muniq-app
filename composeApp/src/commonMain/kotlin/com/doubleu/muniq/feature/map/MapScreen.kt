package com.doubleu.muniq.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.doubleu.muniq.platform.MuniqMap

@Composable
fun MapScreen(
    viewModel: MapViewModel = MapViewModel(),
    onDistrictSelected: (String) -> Unit
) {
    val selectedId by viewModel.selectedDistrictId.collectAsState()

    MuniqMap(
        selectedDistrictId = selectedId,
        onDistrictTapped = { id ->
            viewModel.onDistrictTapped(id)
            onDistrictSelected(id)
        }
    )
}