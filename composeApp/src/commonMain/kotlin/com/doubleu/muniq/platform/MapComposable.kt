package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MuniqMap(
    modifier: Modifier = Modifier,
    onTap: (latitude: Double, longitude: Double) -> Unit = { _, _ -> }
)
