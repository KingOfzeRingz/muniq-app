package com.doubleu.muniq

import androidx.compose.ui.window.ComposeUIViewController
import com.doubleu.muniq.app.MuniqApp
import com.doubleu.muniq.core.di.initKoin
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

fun MainViewController() = ComposeUIViewController {

    initKoin()

    val deviceLang = NSLocale.currentLocale.languageCode

    MuniqApp()
//    HomeScreen()
}