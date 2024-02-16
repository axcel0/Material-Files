package com.example.materialfilejetpackcompose

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.materialfilejetpackcompose.ui.theme.MaterialFileJetpackComposeTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.FileViewModelFactory

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

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            // App is running on TV, load the directory without asking for permissions
            fileViewModel.getFileList(Environment.getExternalStorageDirectory())
        } else {
            // App is not running on TV, request permissions as usual
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    fileViewModel.getFileList(Environment.getExternalStorageDirectory())
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }

            requestPermissionLauncher.launch(arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }

        fileViewModel.currentDirectory.observe(this) {
            if (it != null) {
                title = it.name
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            if (fileViewModel.directoryStack.isNotEmpty()) {
                fileViewModel.getFileList()
            } else {
                finish()
            }
        }

        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(sharedPreferences.getBoolean(DARK_MODE_PREF, isSystemInDarkTheme))
            }

            var isExitDialogShown by remember { mutableStateOf(false) }

            val onDarkModeChange : (Boolean) -> Unit = {
                isDarkTheme = it
                sharedPreferences.edit().putBoolean(DARK_MODE_PREF, it).apply()
            }

            MaterialFileJetpackComposeTheme(isInDarkTheme = isDarkTheme) {
                MyApp(isDarkTheme, onDarkModeChange, fileViewModel)
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


