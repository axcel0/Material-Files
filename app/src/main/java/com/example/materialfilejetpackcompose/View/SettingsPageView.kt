package com.example.materialfilejetpackcompose.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

class SettingsPageView(private val navController: NavController) {
    @Composable
    fun SettingsPage(
        isDarkTheme: Boolean,
        onDarkModeChange: (Boolean) -> Unit
    ) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Settings")
                Spacer(modifier = Modifier.height(20.dp))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onDarkModeChange,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {
                    navController.navigate("home")
                }) {
                    Text("Go back")
                }
            }
        }
    }
}