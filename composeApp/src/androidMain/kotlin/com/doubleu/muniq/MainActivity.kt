package com.doubleu.muniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.doubleu.muniq.core.di.initKoin
import com.doubleu.muniq.presentation.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin()

        setContent {
//            App()
            HomeScreen()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}