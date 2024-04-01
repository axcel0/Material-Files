package com.example.materialfilejetpackcompose.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

class SettingsPageView(private val navController: NavController) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsPage(
        isDarkTheme: Boolean,
        onDarkModeChange: (Boolean) -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate("home") }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth(),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Toggle Light/Dark Mode")
                    Icon (
                        imageVector = if (isDarkTheme) Icons.Filled.NightsStay else Icons.Filled.WbSunny,
                        contentDescription = "Toggle Light/Dark Mode",
                        modifier = Modifier
                            .padding(start = 24.dp, end = 8.dp)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onDarkModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.DarkGray,
                            uncheckedThumbColor = Color.LightGray,
                            checkedTrackColor = Color.DarkGray.copy(alpha = 0.3f),
                            uncheckedTrackColor = Color.LightGray.copy(alpha = 0.3f)
                        )
                    )
                    Column {
                        IconButton(onClick = { navController.navigate("about") }) {
                            Icon(Icons.Filled.Info, contentDescription = "About")
                        }
                    }
                }

            }
        }
    }
}