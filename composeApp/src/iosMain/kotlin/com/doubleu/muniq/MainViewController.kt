package com.doubleu.muniq

import androidx.compose.ui.window.ComposeUIViewController
import com.doubleu.muniq.core.di.initKoin
import com.doubleu.muniq.presentation.home.HomeScreen

fun MainViewController() = ComposeUIViewController {
//    App()

    initKoin()

    HomeScreen()
}