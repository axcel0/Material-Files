package com.example.materialfilejetpackcompose.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavHostController, fileViewModel: FileViewModel) {
    var isExpanded by remember { mutableStateOf(false) }
    val widthAnim by animateDpAsState(targetValue = if (isExpanded) 200.dp else 50.dp, label = "")
    var searchQuery by remember { mutableStateOf("") }
    var isSearchBarVisible by remember { mutableStateOf(false) }
    val selectedFiles = fileViewModel.selectedFiles.observeAsState()

    Surface {
        val focusManager = LocalFocusManager.current

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
                DrawerItem(Icons.Default.Folder, "All Files", isExpanded) {
                    fileViewModel.currentDirectory.value?.let {
                        fileViewModel.loadInternalStorage(it)
                    }
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
            title = {
                if (isSearchBarVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue
                            if (newValue.isNotEmpty()) {
                                fileViewModel.searchFiles(newValue)
                            } else {
                                fileViewModel.currentDirectory.value?.let {
                                    fileViewModel.loadInternalStorage(it)
                                }
                            }
                        },
                        placeholder = { Text("Search") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            isSearchBarVisible = false
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier.onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                //move cursor to left when focus is lost
                                searchQuery = ""
                                fileViewModel.currentDirectory.value?.let {
                                    fileViewModel.loadInternalStorage(it)
                                }
                            }else if (focusState.isFocused){
                                //move cursor to right when focus is gained
                                searchQuery = ""
                            }
                        }
                    )
                } else {
                    Text("Material Files", style = MaterialTheme.typography.bodyMedium)
                }
            },
            actions = {
                IconButton(onClick = {
                    navController.navigate("search")
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
        )

        Column(
            Modifier
                .fillMaxSize()
        ) {
            //distance between top app bar and content
            Spacer(modifier = Modifier.height(60.dp))

            Column(
                Modifier
                    .padding(start = widthAnim)
                    .fillMaxHeight(if (selectedFiles.value!!.isEmpty()) 1f else 0.9f)
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
                    Column {
                        IconButton(
                            onClick = {
                                fileViewModel.deleteFiles()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                        Text(text = "Delete")
                    }
                    Column {
                        IconButton(
                            onClick = {
                                fileViewModel.copyFiles(selectedFiles.value)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CopyAll,
                                contentDescription = "Copy"
                            )
                        }
                        Text(text = "Copy")
                    }
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

