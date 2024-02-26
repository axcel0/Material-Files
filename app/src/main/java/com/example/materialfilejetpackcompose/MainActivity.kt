package com.example.materialfilejetpackcompose

import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.materialfilejetpackcompose.ui.theme.MaterialFileJetpackComposeTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.materialfilejetpackcompose.View.HomePageView
import com.example.materialfilejetpackcompose.View.SearchPageView
import com.example.materialfilejetpackcompose.View.SettingsPageView
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.FileViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFERENCE_THEME = "preference_theme"
        private const val DARK_MODE_PREF = "dark_mode"
    }

    private val sharedPreferences by lazy {
        getSharedPreferences(PREFERENCE_THEME, MODE_PRIVATE)
    }

    private val fileViewModel: FileViewModel by viewModels {
        FileViewModelFactory(applicationContext)
    }

    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            val currentDirectory = fileViewModel.currentDirectory.value
            if (currentDirectory?.parentFile != null) {
                fileViewModel.loadInternalStorage(currentDirectory.parentFile!!)
            }
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val isRunningOnTV = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        fileViewModel.loadStorage(Environment.getExternalStorageDirectory())

        fileViewModel.currentPath.observe(this) {
            title = it
        }

        fileViewModel.currentDirectory.observe(this) {
            title = it.name
        }

        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(sharedPreferences.getBoolean(DARK_MODE_PREF, isSystemInDarkTheme))
            }

            val onDarkModeChange : (Boolean) -> Unit = {
                isDarkTheme = it
                sharedPreferences.edit().putBoolean(DARK_MODE_PREF, it).apply()
            }

            var isExitDialogShown by remember { mutableStateOf(false) }
            BackHandler {
                val currentDirectory = fileViewModel.currentDirectory.value
                val parentFileExists = currentDirectory?.parentFile?.exists() == true

                if (!parentFileExists) {
                    isExitDialogShown = !isExitDialogShown
                } else {
                    fileViewModel.loadStorage(currentDirectory!!.parentFile)
                }
            }

            MaterialFileJetpackComposeTheme(isInDarkTheme = isDarkTheme) {
                // NavHost for navigation
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        val homePageView by lazy { HomePageView(navController, fileViewModel) }
                        homePageView.HomePage()
                    }
                    composable("settings") {
                        val settingsPageView by lazy { SettingsPageView(navController) }
                        settingsPageView.SettingsPage(isDarkTheme, onDarkModeChange)
                    }
                    composable("search") {
                        val searchPageView by lazy { SearchPageView(navController, fileViewModel) }
                        searchPageView.SearchPage()
                    }
                }

                // Exit dialog
                if (isExitDialogShown) {
                    ExitAlertDialog(
                        title = "Exit",
                        message = "Are you sure you want to exit?",
                        onConfirm = {
                            finish()
                        },
                        onDismiss = {
                            isExitDialogShown = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun ExitAlertDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = onConfirm
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun HandleRequestPersimmon() {
        val storagePermissionState = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        LaunchedEffect(storagePermissionState) {
            storagePermissionState.launchPermissionRequest()
        }

        when {
            storagePermissionState.status.isGranted -> {
                // Permission is granted
            }
            storagePermissionState.status.shouldShowRationale -> {
                // Show an explanation to the user
            }
            else -> {
                // Request permission
            }
        }
    }

}

//    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
//    @Composable
//    fun SidebarContent() {
//        var presses by remember { mutableIntStateOf(0) }
//        Column(modifier = Modifier.fillMaxSize()) {
//            Text(
//                text = "Sidebar",
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            )
//            LazyColumn {
//                items(presses + 1) {
//                    Row(
//                        modifier = Modifier.combinedClickable(
//                            onClick = { presses++ },
//                            onLongClick = {
//                                presses = 0
//                            },
//
//                            role = Role.Button,
//                        )
//                    ) {
//                        Text("Item $it")
//                        IconButton(onClick = {
//                            presses++
//                        }) {
//                            Icon(Icons.Filled.Add, contentDescription = "Add")
//                        }
//                    }
//                }
//            }
//        }
//    }


