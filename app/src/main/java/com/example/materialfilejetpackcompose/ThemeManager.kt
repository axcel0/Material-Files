package com.example.materialfilejetpackcompose

import androidx.compose.runtime.mutableStateOf

class ThemeManager {
    var darkTheme = mutableStateOf(false)

    fun toggleTheme() {
        darkTheme.value = !darkTheme.value
    }
}