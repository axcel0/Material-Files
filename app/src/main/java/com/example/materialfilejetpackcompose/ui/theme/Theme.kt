package com.example.materialfilejetpackcompose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

@Composable
fun MaterialFileJetpackComposeTheme(
    isInDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isInDarkTheme) {
        darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80,
            background = DarkGrey,
            onPrimary = LightGrey,
            onSurface = White,
            onBackground = LightGrey,
        )
    } else {
        lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40,
            background = White,
            onPrimary = DarkGrey,
            onSurface = Black,
            onBackground = DarkGrey,
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(),
        content = content
    )
}