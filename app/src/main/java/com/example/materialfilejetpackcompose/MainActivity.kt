package com.example.materialfilejetpackcompose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View.OnFocusChangeListener
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.materialfilejetpackcompose.ViewModel.PersimmonViewModel
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

private fun getVersionName(context: Context): String {
    var versionName = ""
    try {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        versionName = packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionName
}

class MainActivity : ComponentActivity() {
    override fun onDestroy() {
        super.onDestroy()
        fileViewModel.cleanup()
    }

    companion object {
        private const val PREFERENCE_THEME = "preference_theme"
        private const val DARK_MODE_PREF = "dark_mode"
        private const val SEARCH_HISTORY_PREF = "search_history"
    }

    private val sharedPreferences by lazy {
        getSharedPreferences(PREFERENCE_THEME, MODE_PRIVATE)
    }

    private val fileViewModel: FileViewModel by viewModels {
        FileViewModelFactory(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
            HandleRequestPermission()
            HandleMultiplePermissions()

            val selectedFiles = fileViewModel.selectedFiles.collectAsState()
            selectedFiles.value?.let {
                title = it.size.toString()
            }
            val isSystemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(sharedPreferences.getBoolean(DARK_MODE_PREF, isSystemInDarkTheme))
            }

                val onDarkModeChange: (Boolean) -> Unit = {
                    isDarkTheme = it
                    sharedPreferences.edit().putBoolean(DARK_MODE_PREF, it).apply()
                }

                var searchHistoryPref =
                    sharedPreferences.getStringSet(SEARCH_HISTORY_PREF, setOf()) ?: setOf()

                val onSearchHistoryChange: () -> Unit = {
                    val searchHistorySet = fileViewModel.searchHistories.value!!.toSet()
                    sharedPreferences.edit().putStringSet(SEARCH_HISTORY_PREF, searchHistorySet)
                        .apply()
                    searchHistoryPref = searchHistorySet
                }

                var isExitDialogShown by remember { mutableStateOf(false) }
                BackHandler {
                    val hasDirStack = fileViewModel.directoryStack.size > 1
                    if (hasDirStack) {
                        fileViewModel.directoryStack.pop()
                        fileViewModel.loadStorage(fileViewModel.directoryStack.last())
                        fileViewModel.directoryStack.pop()
                    } else {
                        isExitDialogShown = !isExitDialogShown
                    }
                }

                MaterialFileJetpackComposeTheme(isInDarkTheme = isDarkTheme) {
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
                            val searchPageView by lazy {
                                SearchPageView(
                                    navController,
                                    fileViewModel,
                                    searchHistoryPref.toList(),
                                    onSearchHistoryChange,
                                )
                            }
                            searchPageView.SearchPage()
                        }
                        composable("about") {
                            val scrollState = rememberScrollState()
                            val infiniteTransition = rememberInfiniteTransition(label = "")
                            val scrollAmount by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 2000f, // adjust this value based on your content height
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        1000,
                                        easing = LinearEasing
                                    ), // adjust duration based on your preference
                                    repeatMode = RepeatMode.Restart
                                ), label = ""
                            )

                            LaunchedEffect(scrollAmount) {
                                scrollState.animateScrollTo(scrollAmount.toInt())
                            }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .background(MaterialTheme.colorScheme.background),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "This application is developed using Jetpack Compose",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Version: ${getVersionName(this@MainActivity)}",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Developed by: Axel",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

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
            modifier = Modifier.focusable(false),
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

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun HandleRequestPermission() {
        val context = LocalContext.current
        val permissionState = rememberPermissionState(Manifest.permission.MANAGE_EXTERNAL_STORAGE)

        // Check permission status BEFORE displaying the composable
        if (Environment.isExternalStorageManager()) {
            // Permission already granted, proceed with your app logic
            // (Display main content or other composables)
            return // No need to show dialog or anything else
        }

        // Show rationale OR launch system settings ONLY if permission is needed
        AlertDialog(
            onDismissRequest = { Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show()},
            title = { Text("Permission Required") },
            text = { Text("This app needs full access to files and media to function properly. Please grant permission in settings.") },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }) {
                    Text("Open Settings")
                }
            }
        )
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun HandleMultiplePermissions() {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            }
        }
        val multiplePermissionsState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            )
        )

        var showDialog by remember { mutableStateOf(true) }

        LaunchedEffect(multiplePermissionsState) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }

        when {
            multiplePermissionsState.allPermissionsGranted -> {
                // Check if the permissions are allowed all the time
                if (!Environment.isExternalStorageManager()) {
                    // If not, show a dialog to the user
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDialog = true
                            },
                            title = { Text("Permissions needed") },
                            text = { Text("This app needs access to your storage and media files all the time to function properly. Please allow the permissions all the time in the settings.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    // Open settings to allow the user to change the permission
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    launcher.launch(intent)
                                }) {
                                    Text("Open settings")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDialog = true
                                }) {
                                    Text("No")
                                }
                            }
                        )
                    }
                } else {
                    Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
                }
            }

            multiplePermissionsState.shouldShowRationale -> {
                // This is where you explain to the user why your app needs the permissions
                AlertDialog(
                    onDismissRequest = { /*TODO*/ },
                    title = { Text("Permissions needed") },
                    text = { Text("This app needs access to your storage and media files to function properly.") },
                    confirmButton = {
                        TextButton(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {
                // If the user denies the permission request, you can show a message or handle the denial

            }
        }
    }
    @Composable
    fun HandleActivityResult() {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        Button(onClick = {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            launcher.launch(intent)
        }) {
            Text("Open settings")
        }
    }

}


