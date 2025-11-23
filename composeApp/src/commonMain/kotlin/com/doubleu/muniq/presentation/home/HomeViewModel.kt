package com.doubleu.muniq.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val greeting: String
) : ViewModel() {

    private val _state = MutableStateFlow("Loading…")
    val state: StateFlow<String> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // simulate load
            delay(500)
            _state.value = greeting
        }
    }
}
