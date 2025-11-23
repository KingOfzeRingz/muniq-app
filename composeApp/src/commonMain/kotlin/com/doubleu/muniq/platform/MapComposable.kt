package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.doubleu.muniq.core.model.District

@Composable
expect fun MuniqMap(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    districts: List<District> = emptyList(),
    importantMetrics: List<com.doubleu.muniq.core.model.MetricType> = emptyList(),
    ignoredMetrics: List<com.doubleu.muniq.core.model.MetricType> = emptyList(),
    onTap: (latitude: Double, longitude: Double) -> Unit = { _, _ -> },
    onDistrictClick: (District?) -> Unit = {}
)
