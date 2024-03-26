package com.example.materialfilejetpackcompose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.materialfilejetpackcompose.View.HomePageView
import com.example.materialfilejetpackcompose.View.SearchPageView
import com.example.materialfilejetpackcompose.View.SettingsPageView
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.FileViewModelFactory
import com.example.materialfilejetpackcompose.ui.theme.MaterialFileJetpackComposeTheme
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
//            HandleRequestPersimmon()
//            val context = LocalContext.current
//            LaunchedEffect(Unit) {
//                if (!Environment.isExternalStorageManager()) {
//                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
//                        data = Uri.parse("package:${context.packageName}")
//                    }
//                    startActivity(intent)
//                }
//            }
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
    private fun HandleRequestPersimmon() {
        val context = LocalContext.current
        val intent = remember {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }

        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Permission is granted. You can perform your function here.
            } else {
                // Permission is denied. You can handle the denial here.
            }
        }

        SideEffect {
            if (!Environment.isExternalStorageManager()) {
                launcher.launch(intent)
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
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused = interactionSource.collectIsFocusedAsState()
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.focusable(interactionSource = interactionSource),
                    colors = ButtonDefaults.textButtonColors(
                        if (isFocused.value) Color.DarkGray else Color.Transparent
                    )
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused = interactionSource.collectIsFocusedAsState()
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.focusable(interactionSource = interactionSource),
                    colors = ButtonDefaults.textButtonColors(
                        if (isFocused.value) Color.DarkGray else Color.Transparent
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}


