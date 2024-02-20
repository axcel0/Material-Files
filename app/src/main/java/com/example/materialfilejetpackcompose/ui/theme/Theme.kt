package com.example.materialfilejetpackcompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography

@Composable
fun MaterialFileJetpackComposeTheme(
    isInDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isInDarkTheme) {
        darkColorScheme(
            primary = Black,
            secondary = Black,
            tertiary = Pink80,
            background = Black,
            onPrimary = LightGrey,
            onSurface = White,
            onBackground = White,
        )
    } else {
        lightColorScheme(
            primary = White,
            secondary = White,
            tertiary = Pink40
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(),
        content = content
    )
}