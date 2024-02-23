package com.example.materialfilejetpackcompose.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
class SearchPageView(private val navController: NavController, private val fileViewModel: FileViewModel) {
    @Composable
    fun SearchPage() {
        Column {
            Surface {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.secondary,
                    ),
                    title = {},
                    actions = {
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BackButton()
                            SearchInput()
                            MenuButton()
                        }
                    },
                )

                // TODO: Search filter

            }

            SearchResults()
        }
    }

    @Composable
    fun BackButton() {
        IconButton(
            onClick = {
                navController.navigate("home")
            },
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back"
            )
        }
    }

    @Composable
    fun SearchInput() {
        val focusManager = LocalFocusManager.current
        var searchQuery by remember { mutableStateOf("") }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.isNotEmpty()) {
                    fileViewModel.searchFiles(query)
                }
            },
            placeholder = { Text("Search..") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                },
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .size(55.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        //move cursor to left when focus is lost
                        searchQuery = ""
                        fileViewModel.currentDirectory.value?.let {
                            fileViewModel.loadInternalStorage(it)
                        }
                    } else if (focusState.isFocused) {
                        //move cursor to right when focus is gained
                        searchQuery = ""
                    }
                }
        )
    }

    @Composable
    fun MenuButton() {
        var isMenuVisible by remember { mutableStateOf(false) }
        Box {
            IconButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    isMenuVisible = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            }

            DropdownMenu(
                expanded = isMenuVisible,
                onDismissRequest = { isMenuVisible = false }
            ) {
                DropdownMenuItem(onClick = {
                    //TODO: Implement Search History Settings
                }){
                    Text("Search Settings")
                }
            }
        }
    }

    fun SearchHistory() {

    }

    @Composable
    fun SearchResults() {
        val searchResults by fileViewModel.searchResults.observeAsState(emptyList())

        LazyColumn {
            items(searchResults) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = file.name)
                }
            }
        }
    }
}