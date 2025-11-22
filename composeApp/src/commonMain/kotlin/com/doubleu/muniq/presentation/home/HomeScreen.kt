package com.doubleu.muniq.presentation.home

import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.koin.compose.koinInject

@Composable
fun HomeScreen() {
    val vm: HomeViewModel = koinInject()
    val greeting by vm.state.collectAsState()

    Surface {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
