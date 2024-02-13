package com.example.materialfilejetpackcompose

import android.util.Log
import androidx.compose.runtime.mutableStateOf

class ThemeManager {
    var darkTheme = mutableStateOf(false)

    fun toggleTheme() {
        darkTheme.value = !darkTheme.value
        Log.d("ThemeManager", "Dark theme is now: ${darkTheme.value}")
    }
}