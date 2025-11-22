package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable

@Composable
expect fun MuniqMap(
    selectedDistrictId: String?,
    onDistrictTapped: (String) -> Unit
)
