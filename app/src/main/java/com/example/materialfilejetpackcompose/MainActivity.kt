package com.example.materialfilejetpackcompose

import android.content.Context
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
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : ComponentActivity() {

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

        fileViewModel.selectedFiles.observe(this) {
            if (it != null) {
                title = it.size.toString()
            }
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

            val onSearchHistoryChange : () -> Unit = {
                val searchHistorySet = fileViewModel.searchHistories.value?.toSet()
                sharedPreferences.edit().putStringSet(SEARCH_HISTORY_PREF, searchHistorySet).apply()
            }

            val searchHistoryPref = sharedPreferences.getStringSet(SEARCH_HISTORY_PREF, setOf()) ?: setOf()

            var isExitDialogShown by remember { mutableStateOf(false) }
            BackHandler {
                Toast.makeText(this, fileViewModel.directoryStack.size.toString(), Toast.LENGTH_SHORT).show()
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
                        val searchPageView by lazy {
                            SearchPageView(
                                navController,
                                fileViewModel,
                                searchHistoryPref.toList(),
                                onSearchHistoryChange
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
                                animation = tween(1000, easing = LinearEasing), // adjust duration based on your preference
                                repeatMode = RepeatMode.Restart
                            ), label = ""
                        )

                        LaunchedEffect(scrollAmount) {
                            scrollState.animateScrollTo(scrollAmount.toInt())
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "This application is developed using Jetpack Compose",
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Version: ${getVersionName(this@MainActivity)}",
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Developed by: Axel",
                                textAlign = TextAlign.Center
                            )
                        }
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

    override fun onDestroy() {
        super.onDestroy()
        fileViewModel.cleanup()
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


