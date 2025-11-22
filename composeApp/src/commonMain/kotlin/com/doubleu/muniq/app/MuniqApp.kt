package com.doubleu.muniq.app

import Navigator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.doubleu.muniq.Greeting
import com.doubleu.muniq.app.navigation.NavGraph
import com.doubleu.muniq.core.designsystem.MuniqTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import muniq.composeapp.generated.resources.Res
import muniq.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun MuniqApp() {
    MuniqTheme {
        val navigator = remember { Navigator() }
        NavGraph(navigator = navigator)
    }
}