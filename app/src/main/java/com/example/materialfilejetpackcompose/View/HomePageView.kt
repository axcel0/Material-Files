package com.example.materialfilejetpackcompose.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel

class HomePageView(private val navController: NavHostController, private val fileViewModel: FileViewModel) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomePage() {
        var isExpanded by remember { mutableStateOf(false) }
        val widthAnim by animateDpAsState(
            targetValue = if (isExpanded) 200.dp else 50.dp,
            label = ""
        )
        val selectedFiles = fileViewModel.selectedFiles.observeAsState()
        var isDropdownMenuVisible by remember { mutableStateOf(false) }
        var shouldShowNewFolderDialog by remember { mutableStateOf(false) }
        var filesToCopy = fileViewModel.filesToCopy.observeAsState()
        var ableToPaste by remember { mutableStateOf(false) }
        var shouldShowFileInfo by remember { mutableStateOf(false) }
        var fileInfo: String by remember { mutableStateOf("") }

        Surface {
            val focusManager = LocalFocusManager.current
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(widthAnim)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
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
                    DrawerItem(Icons.Default.Folder, "All Files", isExpanded) {
                        val homeDir = fileViewModel.getHomeDirectory()
                        fileViewModel.loadStorage(homeDir)
                    }
                    DrawerItem(Icons.Default.Photo, "Photos", isExpanded) {
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.loadPhotosOnly(it)
                        }
                    }
                    DrawerItem(Icons.Default.VideoLibrary, "Videos", isExpanded) {
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.loadVideosOnly(it)
                        }
                    }
                    DrawerItem(Icons.Default.AudioFile, "Audios", isExpanded) {
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.loadAudiosOnly(it)
                        }
                    }
                }
                DrawerItem(Icons.Default.Settings, "Settings", isExpanded) {
                    navController.navigate("settings")
                }
            }

            TopAppBar(
                modifier = Modifier
                    .padding(start = widthAnim)
                    .fillMaxWidth(),
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Material Files", style = MaterialTheme.typography.titleLarge)
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

                        DropdownMenu(expanded = isDropdownMenuVisible, onDismissRequest = {}) {
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
                }

                BottomAppBar(
                    modifier = Modifier
                        .padding(start = widthAnim)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val actions = listOf(
                            Pair(Icons.Default.Delete, "Delete") to {
                                fileViewModel.deleteFiles()
                            },

                            if (!ableToPaste) Pair(Icons.Default.ContentCut, "Cut") to {
                                ableToPaste = true
                                fileViewModel.cutFiles(selectedFiles.value!!)
                            } else Pair(null, "") to {},

                            if (ableToPaste) Pair(Icons.Default.ContentPaste, "Paste") to {
                                ableToPaste = false
                                fileViewModel.pasteFiles(fileViewModel.currentDirectory.value!!)
                            }
                            else Pair(Icons.Default.CopyAll, "Copy") to {
                                ableToPaste = true
                                fileViewModel.copyFiles(selectedFiles.value!!)
                            },

                            /*if (selectedFiles.value!!.count() == 1) Pair(Icons.Default.Edit, "Rename") to {
                            }
                            else Pair(null, "") to {}*/

                            if (selectedFiles.value!!.count() == 1) Pair(Icons.Default.Info, "Info") to {
                                shouldShowFileInfo = true
                                fileInfo = fileViewModel.getFileInfo(selectedFiles.value!!.first())
                            } else Pair(null, "") to {}
                        )

                        actions.forEach { (iconData, action) ->
                            iconData.first?.let { ActionButton(imageVector = it, text = iconData.second, onClick = action) }
                        }
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

        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Text(text = "Create new folder", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
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

                Row {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val currentDir = fileViewModel.currentDirectory.value
                            if (currentDir != null) {
                                fileViewModel.createNewFolder(currentDir.absolutePath, folderName)
                                onCancel()
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }

    @Composable
    fun FileInfoDialog(fileInfo: String, onCancel: () -> Unit) {
        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Text(text = "File Info", style = MaterialTheme.typography.titleLarge)
                Text(text = fileInfo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

