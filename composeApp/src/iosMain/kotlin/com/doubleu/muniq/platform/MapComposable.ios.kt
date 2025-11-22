package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text

@Composable
actual fun MuniqMap(
    modifier: Modifier,
    isDarkTheme: Boolean,
    onTap: (Double, Double) -> Unit
) {
    // Temporary placeholder until iOS map is implemented
    Text("iOS Map Placeholder")
}