package com.doubleu.muniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.doubleu.muniq.app.MuniqApp
import com.doubleu.muniq.core.di.initKoin
import com.doubleu.muniq.core.localization.Language
import com.doubleu.muniq.core.localization.Localization
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin()

        val deviceLang = Locale.getDefault().language
        Localization.setLanguage(
            when (deviceLang) {
                "de" -> Language.DE
                "ru" -> Language.RU
                else -> Language.EN
            }
        )


        setContent {
            MuniqApp()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MuniqApp()
}