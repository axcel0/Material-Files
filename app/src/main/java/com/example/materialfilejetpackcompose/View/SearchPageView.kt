package com.example.materialfilejetpackcompose.View

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.materialfilejetpackcompose.MainActivity
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalMaterial3Api::class)
class SearchPageView(private val navController: NavController,
                     private val fileViewModel: FileViewModel,
                     searchHistoryPref : List<String>,
                     private val onSearchValueChanged: () -> Unit ) {

    init {
        fileViewModel.searchHistories.value = searchHistoryPref
    }

    @Composable
    fun SearchPage() {
        var searchQuery by remember { mutableStateOf("") }
        val onSearchValueChanged = { value: String -> searchQuery = value }
        val progressDialogVisible by fileViewModel.searchLoadProgress.observeAsState(0)

        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxHeight()
        ) {
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
                            SearchInput(searchQuery, onSearchValueChanged)
                            MenuButton()
                        }
                    },
                )

                // TODO: Search filter
                // フィルター：　期間（昨日、過去7日間、過去３０日間）
                //　タイプ：　画像、動画、オーディオ、ドキュメント、インストールファイル、圧縮ファイル）
                // これガチでしんどいな

            }
            if (searchQuery.isEmpty()) {
                SearchHistory(onSearchValueChanged)
            } else {
                SearchResults()

                if (progressDialogVisible <= 0) {
                    return
                }
                CircularProgressAlert()
            }
        }
    }

    @Composable
    fun BackButton() {
        IconButton(
            onClick = {
                navController.popBackStack()
                navController.popBackStack()
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
    fun MenuButton() {
        var isMenuVisible by remember { mutableStateOf(false) }
        var isDialogVisible by remember { mutableStateOf(false) }
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
                DropdownMenuItem(
                    onClick = {
                        isMenuVisible = false
                        isDialogVisible = true
                    },
                    text = { Text("Clear Search History") }
                )
            }

            if (isDialogVisible) {
                ClearSearchHistoryDialog(
                    title = "Clear Search History",
                    message = "Are you sure you want to clear the search history?",
                    onConfirm = {
                        fileViewModel.searchHistories.value = emptyList()
                        onSearchValueChanged()
                        isDialogVisible = false
                    },
                    onDismiss = {
                        isDialogVisible = false
                    }
                )
            }
        }
    }

    @Composable
    fun SearchInput(searchQuery:String, onSearchValueChanged: (String) -> Unit){
        val focusManager = LocalFocusManager.current
        val searchHistories = fileViewModel.searchHistories.observeAsState(emptyList()).value

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchValueChanged,
            placeholder = { Text("Search..") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()

                    if (searchQuery.isNotEmpty()) {
                        fileViewModel.searchFiles(searchQuery)

                        if (!searchHistories.contains(searchQuery)) {
                            fileViewModel.searchHistories.value = searchHistories.toMutableList().apply {
                                add(searchQuery)
                            }

                            onSearchValueChanged()
                        }
                    }
                },
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .size(55.dp)
        )
    }

    @Composable
    fun SearchHistory(onSearchValueChanged: (String) -> Unit) {
        val searchHistories = fileViewModel.searchHistories.observeAsState(emptyList()).value
        val focusManager = LocalFocusManager.current
        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxHeight(),
        ) {
            items(searchHistories) { query ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable {
                            focusManager.clearFocus()
                            if (query.isNotEmpty()) {
                                fileViewModel.searchFiles(query)
                                if (!searchHistories.contains(query)) {
                                    fileViewModel.searchHistories.value = searchHistories
                                        .toMutableList()
                                        .apply {
                                            add(query)
                                        }
                                }

                                onSearchValueChanged(query) // Update the search query in the TextField
                            }
                        }
                ) {
                    Text(text = query, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SearchResults() {
        val searchResults by fileViewModel.searchResults.observeAsState(emptyList())

        LazyColumn(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxHeight(),
        
        ) {
            items(searchResults) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (file.isDirectory) {
                                    fileViewModel.loadStorage(file)
                                    navController.navigate("home")
                                } else {
                                    fileViewModel.openMediaFile(file)
                                }
                            },
                            onLongClick = {
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        file.isDirectory -> {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Folder Icon",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFFFA400)
                            )
                        }
                        fileViewModel.isFilePhoto(file) -> {
                            // Display image thumbnail
                            val imageBitmap = rememberAsyncImagePainter(file.path)
                            Image(
                                painter = imageBitmap,
                                contentDescription = "Image Thumbnail",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        fileViewModel.isFileVideo(file) -> {
                            // Display video thumbnail
                            val thumbnail = rememberVideoThumbnail(file.path)
                            Image(
                                bitmap = thumbnail!!.asImageBitmap(),
                                contentDescription = "Video Thumbnail",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        fileViewModel.isFileAudio(file) -> {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Audio Icon",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFFFA400)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "File Icon",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFFFA400)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = file.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
        }
    }
    @Composable
    fun rememberVideoThumbnail(filePath: String): Bitmap? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(filePath)
        val bitmap = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        mediaMetadataRetriever.release()
        return bitmap
    }

    @Composable
    fun ClearSearchHistoryDialog(
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
                    Text("Clear")
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

    @Composable
    private fun CircularProgressAlert() {
        Dialog(onDismissRequest = { }) {
            Row {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp),
                )
                Text(text = "検索しています…")
            }
        }
    }
}