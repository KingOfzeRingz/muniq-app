package com.doubleu.muniq.presentation.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeViewModel(
    private val greeting: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow("Loading…")
    val state: StateFlow<String> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // simulate load
            kotlinx.coroutines.delay(500)
            _state.value = greeting
        }
    }
}
