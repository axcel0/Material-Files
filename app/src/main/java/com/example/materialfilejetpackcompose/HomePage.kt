package com.example.materialfilejetpackcompose

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MyApp(themeManager: ThemeManager) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomePage(navController) }
        composable("settings") { SettingsPage(navController, themeManager) }
    }
}

@Composable
fun SettingsPage(navController: NavController, themeManager: ThemeManager) {
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
                checked = themeManager.darkTheme.value,
                onCheckedChange = { themeManager.toggleTheme() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavHostController) {
    var isExpanded by remember { mutableStateOf(false) }
    val widthAnim by animateDpAsState(targetValue = if (isExpanded) 200.dp else 64.dp, label = "")

    Surface {
        Box {
            TopAppBar(
                modifier = Modifier
                    .padding(start = widthAnim)
                    .fillMaxWidth(),
                title = { Text("Material Files", color = MaterialTheme.colorScheme.onPrimary) },
                colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.tertiary)
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .animateContentSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val context = LocalContext.current
                Button(onClick = {
                    Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Click me")
                }
            }
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(widthAnim)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .onFocusChanged { focusState ->
                        isExpanded = focusState.isFocused
                    }
                    .animateContentSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Default.Menu,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp)
                        .clickable(
                            onClick = {
                                isExpanded = !isExpanded
                            }
                        ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Column {
                    Modifier
                        .padding(top = 10.dp, start = 10.dp)
                    DrawerItem(Icons.Default.Folder, "All Files", isExpanded) {}
                    DrawerItem(Icons.Default.Photo, "Photos", isExpanded) {}
                    DrawerItem(Icons.Default.VideoLibrary, "Videos", isExpanded) {}
                    DrawerItem(Icons.Default.AudioFile, "Audios", isExpanded) {}
                }
                DrawerItem(Icons.Default.Settings, "Settings", isExpanded) {
                    navController.navigate("settings")
                }

            }
        }
    }
}


@Composable
fun DrawerItem(icon: ImageVector, title: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

