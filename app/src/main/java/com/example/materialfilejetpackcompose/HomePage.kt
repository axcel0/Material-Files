package com.example.materialfilejetpackcompose

import android.app.LauncherActivity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.materialfilejetpackcompose.View.ContentView
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel

@Composable
fun MyApp(isDarkTheme: Boolean, onDarkModeChange: (Boolean) -> Unit, fileViewModel: FileViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomePage(navController, fileViewModel) }
        composable("settings") { SettingsPage(navController, isDarkTheme, onDarkModeChange) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(navController: NavController, isDarkTheme: Boolean, onDarkModeChange: (Boolean) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
//            item {
//                ListItem(
//                    text = "Dark Mode",
//                    icon = Icons.Default.Nightlight,
//                    onClick = {
//                        onDarkModeChange(!isDarkTheme)
//                    },
//                    trailing = {
//                        Switch(
//                            checked = isDarkTheme,
//                            onCheckedChange = onDarkModeChange
//                        )
//                    }
//                )
//            }
            item {
                Text(
                    text = "About this app",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            item {
                Text(
                    text = "This app is a File Explorer made with Kotlin Jetpack Compose and is used for personal purposes."
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavHostController, fileViewModel: FileViewModel) {
    var isExpanded by remember { mutableStateOf(false) }
    val widthAnim by animateDpAsState(targetValue = if (isExpanded) 200.dp else 50.dp, label = "")

    Surface {
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
        ) {
            //distance between top app bar and content
            Spacer(modifier = Modifier.height(60.dp))

            Column {
                val contentView by lazy { ContentView(fileViewModel) }
                contentView.Content()
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
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable(
                        onClick = {
                            isExpanded = !isExpanded
                        }
                    ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Column {
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

@Composable
fun DrawerItem(icon: ImageVector, title: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
            enter = fadeIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

