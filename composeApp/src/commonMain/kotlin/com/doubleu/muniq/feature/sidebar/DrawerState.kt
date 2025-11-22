package com.doubleu.muniq.feature.sidebar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DrawerState {
    private val _isOpen = MutableStateFlow(false)
    val isOpen: StateFlow<Boolean> = _isOpen

    fun open() { _isOpen.value = true }
    fun close() { _isOpen.value = false }
    fun toggle() { _isOpen.value = !_isOpen.value }
}