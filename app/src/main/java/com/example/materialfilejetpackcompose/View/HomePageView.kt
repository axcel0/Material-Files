package com.example.materialfilejetpackcompose.View

import android.os.storage.StorageVolume
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import java.io.File

class HomePageView(private val navController: NavHostController, private val fileViewModel: FileViewModel) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomePage() {
        val selectedFiles = fileViewModel.selectedFiles.collectAsState()
        val filesToCopy = fileViewModel.filesToCopy.observeAsState()

        var isExpanded by remember { mutableStateOf(false) }
        var fileInfo: String by remember { mutableStateOf("") }
        var oldFile by remember { mutableStateOf<File?>(null) }
        var ableToPaste by remember { mutableStateOf(false) }

        var isDropdownMenuVisible by remember { mutableStateOf(false) }
        var shouldShowNewFolderDialog by remember { mutableStateOf(false) }
        val shouldShowFileCanvasOperation by remember { mutableStateOf(false) }

        var shouldShowFileInfo by remember { mutableStateOf(false) }
        var shouldShowRenameDialog by remember { mutableStateOf(false) }
        var shouldShowDeleteDialog by remember { mutableStateOf(false) }
        var externalDevices by remember { mutableStateOf<List<StorageVolume>>(emptyList()) }

        val widthAnim by animateDpAsState(
            targetValue = if (isExpanded) 200.dp else 50.dp,
            animationSpec = tween(durationMillis = 10), label = "anime"
        )

        val widthAnime by animateDpAsState(
            targetValue = if (shouldShowFileCanvasOperation) 200.dp else 0.dp,
            animationSpec = tween(durationMillis = 50), label = "anime",
        )

        Surface(
            modifier = Modifier.focusable(false)
        ) {
            Column(
                Modifier
                    .width(widthAnim)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp)
                    .focusable(false)
                    .onFocusChanged { focusState ->
                        isExpanded = focusState.isFocused
                    },
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    Modifier
                        .fillMaxHeight(0.9f)
                        .width(widthAnim)
                        .focusable(false),
                    verticalArrangement = Arrangement.Center,
                ) {
                    DrawerItem(Icons.Default.Folder, "All Files", isExpanded) {
                        val homeDir = fileViewModel.getHomeDirectory()
                        fileViewModel.directoryStack.clear()
                        fileViewModel.loadStorage(homeDir)
                    }
                    DrawerItem(Icons.Default.Photo, "Photos", isExpanded) {
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.directoryStack.clear()
                            fileViewModel.loadPhotosOnly(it)
                        }
                    }
                    DrawerItem(Icons.Default.VideoLibrary, "Videos", isExpanded) {
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.directoryStack.clear()
                            fileViewModel.loadVideosOnly(it)
                        }
                    }
                    DrawerItem(Icons.Default.AudioFile, "Audios", isExpanded) {
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.directoryStack.clear()
                            fileViewModel.loadAudiosOnly(it)
                        }
                    }
                    externalDevices = fileViewModel.getExternalStorageDevices()
                    externalDevices.forEach { storageVolume ->
                        val storageDescription = storageVolume.getDescription(LocalContext.current)
                        DrawerItem(Icons.Default.Usb, storageDescription, isExpanded) {
                            fileViewModel.directoryStack.clear()
                            fileViewModel.loadedExternalDevice = storageDescription
                            fileViewModel.loadStorage(File(storageVolume.directory?.path ?: fileViewModel.getHomeDirectory().path))
                        }
                    }
                }
                DrawerItem(Icons.Default.Settings, "Settings", isExpanded) {
                    navController.navigate("settings")
                }
            }

            CenterAlignedTopAppBar(
                modifier = Modifier
                    .padding(start = widthAnim)
                    .fillMaxWidth(),
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Material Files",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.focusable(false)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("search")
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    Box {
                        IconButton(onClick = {
                            isDropdownMenuVisible = !isDropdownMenuVisible
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = isDropdownMenuVisible,
                            onDismissRequest = { isDropdownMenuVisible = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New folder") },
                                onClick = {
                                    shouldShowNewFolderDialog = true
                                    isDropdownMenuVisible = false
                                }
                            )
                        }
                    }
                },
            )

            Column(Modifier.fillMaxSize()) {
                //distance between top app bar and content
                Spacer(modifier = Modifier.height(60.dp))
                Column(
                    Modifier
                        .padding(start = widthAnim)
                        .fillMaxHeight(if (selectedFiles.value!!.isEmpty() && filesToCopy.value!!.isEmpty()) 1f else 0.9f)
                ) {
                    val contentView by lazy { ContentView(fileViewModel) }
                    contentView.Content()
                    if (shouldShowRenameDialog && oldFile != null) {
                        RenameFileDialog(oldFile = oldFile!!, onRename = {
                            fileViewModel.renameFile(oldFile!!, it)
                            shouldShowRenameDialog = false
                        }) {
                            shouldShowRenameDialog = false
                        }
                    } else if (shouldShowDeleteDialog) {
                        DeleteFileDialog(onDelete = {
                            fileViewModel.deleteFiles()
                            shouldShowDeleteDialog = false
                        }) {
                            shouldShowDeleteDialog = false
                        }
                    }
                }

                BottomAppBar(
                    modifier = Modifier
                        .padding(start = widthAnim)
                        .fillMaxHeight()
                        .align(Alignment.End)
                ) {
                    val isSingleFileSelected = selectedFiles.value!!.count() == 1

                    val actions = listOfNotNull(
                        if (selectedFiles.value!!.isNotEmpty()) Pair(Icons.Default.Delete, "Delete") to {
                            shouldShowDeleteDialog = true
                        } else null,

                        if (!ableToPaste) Pair(Icons.Default.ContentCut, "Cut") to {
                            ableToPaste = true
                            fileViewModel.cutFiles(selectedFiles.value!!)
                        } else null,

                        if (ableToPaste) Pair(Icons.Default.ContentPaste, "Paste") to {
                            ableToPaste = false
                            fileViewModel.pasteFiles(fileViewModel.currentDirectory.value!!)
                        } else Pair(Icons.Default.CopyAll, "Copy") to {
                            ableToPaste = true
                            fileViewModel.copyFiles(selectedFiles.value!!)
                        },

                        if (isSingleFileSelected) Pair(Icons.Default.Info, "Info") to {
                            shouldShowFileInfo = true
                            fileInfo = fileViewModel.getFileInfo(selectedFiles.value!!.first())
                        } else null,

                        if (isSingleFileSelected) Pair(Icons.Default.Edit, "Rename") to {
                            oldFile = selectedFiles.value!!.first()
                            shouldShowRenameDialog = true
                        } else null,

//                        Pair(Icons.Default.Cancel, "Cancel") to {
//                            fileViewModel.cancelOperation()
//                        }
                    )

                    actions.forEach { (iconData, action) ->
                        ActionButton(imageVector = iconData.first, text = iconData.second, onClick = action)
                    }
                }

                if (shouldShowNewFolderDialog) {
                    NewFolderDialog { shouldShowNewFolderDialog = false }
                }

                if (shouldShowFileInfo) {
                    FileInfoDialog(fileInfo) { shouldShowFileInfo = false }
                }
            }
        }
    }

    @Composable
    fun ActionButton(imageVector: ImageVector, text: String, onClick: () -> Unit) {
        Column {
            IconButton(onClick = onClick) {
                Icon(imageVector = imageVector, contentDescription = text)
            }
            Text(text = text)
        }
    }

    @Composable
    fun DrawerItem(icon: ImageVector, title: String, expanded: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusable(false)
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
                exit = fadeOut()
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f)
                )
            }
        }
    }

    @Composable
    fun NewFolderDialog(onCancel: () -> Unit = {}) {
        var folderName by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(text = "Create new folder", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            val currentDir = fileViewModel.currentDirectory.value
                            if (currentDir != null) {
                                fileViewModel.createNewFolder(currentDir.absolutePath, folderName)
                                onCancel()
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val currentDir = fileViewModel.currentDirectory.value
                    if (currentDir != null) {
                        fileViewModel.createNewFolder(currentDir.absolutePath, folderName)
                        onCancel()
                    }
                }) {
                    Text("Create")
                }
            }
        )
    }

    @Composable
    fun RenameFileDialog(oldFile: File, onRename: (String) -> Unit, onCancel: () -> Unit = {}) {
        var newName by remember {
            mutableStateOf(
                if (oldFile.isDirectory) {
                    oldFile.nameWithoutExtension
                } else {
                    "${oldFile.nameWithoutExtension}.${oldFile.extension}"
                }
            )
        }
        val focusManager = LocalFocusManager.current

        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(text = "Rename File", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onRename(newName)
                            fileViewModel.renameFile(oldFile, newName)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onRename(newName)
                    fileViewModel.renameFile(oldFile, newName)
                }) {
                    Text("Rename")
                }
            }
        )
    }

    @Composable
    fun FileInfoDialog(fileInfo: String, onCancel: () -> Unit) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(text = "File Info", style = MaterialTheme.typography.titleLarge) },
            text = { Text(text = fileInfo, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("OK")
                }
            }
        )
    }

    @Composable
    fun DeleteFileDialog(onDelete: () -> Unit, onCancel: () -> Unit) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(text = "Delete File", style = MaterialTheme.typography.titleLarge) },
            text = { Text(text = "Are you sure you want to delete this file?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        )
    }
}

