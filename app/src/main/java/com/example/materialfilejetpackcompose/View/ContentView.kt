package com.example.materialfilejetpackcompose.View

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.SortType
import java.io.File

class ContentView(private val fileViewModel: FileViewModel) {

    @Composable
    fun Content() {
        val files by fileViewModel.files.observeAsState(emptyList())
        val context = LocalContext.current
        var isGridView by remember { mutableStateOf(true) }
        var sortType by remember { mutableStateOf(SortType.NAME) }
        var isAscending by remember { mutableStateOf(true) }
        val currentDirectory by fileViewModel.currentDirectory.observeAsState()

        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                val pathComponents = currentDirectory?.path?.split("/") ?: listOf()
                Row {
                    pathComponents.forEachIndexed { index, component ->
                        val isRootDirectory = index == 0 && component.isEmpty()
                        if (isRootDirectory) return@forEachIndexed

                        val isHomeDir = pathComponents.getOrNull(index) == "0"
                        val isExternalStorageDir = fileViewModel.isExternalStorage() && index > 0 && pathComponents.getOrNull(index - 1) == "storage"
                        val newPath = getNewPath(pathComponents, index)
                        val isNormalDirectory = fileViewModel.isExternalStorage() && index > 2 || !fileViewModel.isExternalStorage() && index > 3

                        when {
                            isHomeDir -> CreateHomeDirButton(pathComponents.subList(0, index + 1).joinToString("/"), index, pathComponents)
                            isExternalStorageDir -> CreateExternalStorageDirButton(newPath, fileViewModel, index, pathComponents)
                            isNormalDirectory -> CreateDefaultDirButton(newPath, component, index, pathComponents, fileViewModel)
                        }
                    }
                }

                var isSortExpanded by remember { mutableStateOf(false) }
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        IconButton(onClick = { isSortExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = isSortExpanded,
                            onDismissRequest = { isSortExpanded = false }
                        ) {
                            val list =
                                listOf(
                                    "Sort by name",
                                    "Sort by date",
                                    "Sort by size",
                                    "Sort by directory"
                                )
                            list.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        isSortExpanded = false
                                        when (item) {
                                            "Sort by name" -> sortType = SortType.NAME
                                            "Sort by date" -> sortType = SortType.DATE
                                            "Sort by size" -> sortType = SortType.SIZE
                                            "Sort by directory" -> sortType = SortType.TYPE
                                        }
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { isAscending = !isAscending }
                    ) {
                        Icon(
                            imageVector = if (isAscending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = if (isAscending) "Ascending" else "Descending"
                        )
                    }

                    IconButton(
                        onClick = { isGridView = !isGridView }
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.ViewModule,
                            contentDescription = if (isGridView) "Grid View" else "List View"
                        )
                    }
                }
            }
            files?.let {
                GridOrListView(
                    files = it,
                    context = context,
                    fileViewModel = fileViewModel,
                    isGridView = isGridView,
                    sortType = sortType,
                    isAscending = isAscending,
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalTvMaterial3Api::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    fun FileItem(file: File, context: Context, fileViewModel: FileViewModel, isGridView: Boolean, onFileSelected: (File) -> Unit,
                 onFileDeselected: (File) -> Unit) {
        val selectedFiles by fileViewModel.selectedFiles.collectAsState(emptySet())
        var isSelected = selectedFiles!!.contains(file)
        val isDarkMode = isSystemInDarkTheme()
        val maxChars = if (isGridView) 16 else 32
        val displayName = if (file.name.length > maxChars) {
            file.name.substring(0, maxChars) + "..."
        } else {
            file.name
        }

        ListItem(
            headlineContent = {
                Text(
                    text = displayName,
                    fontSize = if (isGridView) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            leadingContent =  {
                if (isSelected) {
                    Checkbox(
                        checked = true,
                        onCheckedChange = {
                        },
                        modifier = Modifier
                            .focusable(false)
                            .scale(if (isGridView) 1.5f else 1.25f)
                            .padding(if (isGridView) 4.dp else 0.dp)
                        ,


                        enabled = false,
                        colors = CheckboxDefaults.colors(
                            disabledCheckedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.inversePrimary
                        )

                    )
                    return@ListItem
                }
                Box(
                    modifier = Modifier.padding(if (isGridView) 0.dp else 8.dp)
                ){
                    when {
                        file.isDirectory -> {
                            Icon(
                                imageVector = Icons.Filled.Folder,
                                contentDescription = "Folder",
                                modifier = if (isGridView) Modifier.size(56.dp) else Modifier.size(32.dp),
                                tint = Color(0xFFFFA400)
                            )
                        }
                        fileViewModel.isFilePhoto(file) -> {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(context)
                                        .data(file)
                                        .build()
                                ),
                                contentDescription = "Photo",
                                modifier = if (isGridView) Modifier.size(72.dp) else Modifier.size(24.dp)
                            )
                        }
                        fileViewModel.isFileAudio(file) -> {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Audio",
                                modifier = if (isGridView) Modifier.size(72.dp) else Modifier.size(24.dp),
                                tint = Color(0xFF757575)
                            )
                        }
                        fileViewModel.isFileVideo(file) -> {
                            // Display video thumbnail
                            val thumbnail = rememberVideoThumbnail(file.path)
                            Image(
                                bitmap = thumbnail!!.asImageBitmap(),
                                contentDescription = "Video Thumbnail",
                                modifier = if (isGridView) Modifier.size(72.dp) else Modifier.size(24.dp),
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = "File",
                                modifier = if (isGridView) Modifier.size(72.dp) else Modifier.size(24.dp),
                                tint = Color(0xFF757575)
                            )
                        }
                    }
                }

            },


            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        isSelected = !isSelected
                        if (isSelected) {
                            fileViewModel.addSelectedFile(file)
                        } else {
                            fileViewModel.removeSelectedFile(file)
                        }
                    },
                    onClick = {
                        if (selectedFiles.isNullOrEmpty()) {
                            if (file.isDirectory) {
                                fileViewModel.loadStorage(file)
                            } else if (fileViewModel.isFilePhoto(file) || fileViewModel.isFileAudio(
                                    file
                                ) || fileViewModel.isFileVideo(file)
                            ) {
                                fileViewModel.openMediaFile(file)
                            }
                        } else {
                            isSelected = !isSelected
                            if (isSelected) {
                                fileViewModel.addSelectedFile(file)
                            } else {
                                fileViewModel.removeSelectedFile(file)
                            }
                        }
                    },

                    )
                .padding(if (isGridView) 8.dp else 16.dp),

//            trailingContent = {
//                Checkbox(checked = isSelected, onCheckedChange = {
//                    isSelected = it
//                    if (isSelected) {
//                        fileViewModel.addSelectedFile(file)
//                    } else {
//                        fileViewModel.removeSelectedFile(file)
//                    }
//                },
//                        modifier = Modifier
//                        .focusable(false)
//                    .scale(if (isGridView) 1.5f else 1.25f)
//                    .padding(if (isGridView) 4.dp else 0.dp)
//                    ,
//
//
//                    enabled = false,
//                    colors = CheckboxDefaults.colors(
//                        disabledCheckedColor = MaterialTheme.colorScheme.primary,
//                        checkmarkColor = MaterialTheme.colorScheme.inversePrimary
//                    )
//                    )
//            }

        )
    }

    @Composable
    fun GridOrListView(
        files: List<File>,
        context: Context,
        fileViewModel: FileViewModel,
        isGridView: Boolean,
        sortType: SortType,
        isAscending: Boolean
    ) {
        val refreshKey by remember { mutableIntStateOf(0) }
        if (isGridView) {
            TvLazyVerticalGrid(
                columns = TvGridCells.Adaptive(200.dp),
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                state = rememberTvLazyGridState(refreshKey),
            ) {
                var sortedFiles : List<File>
                sortedFiles = when (sortType) {
                    SortType.NAME -> { files.sortedBy { it.name } }
                    SortType.DATE -> { files.sortedBy { it.lastModified() } }
                    SortType.SIZE -> { files.sortedBy { it.length() } }
                    SortType.TYPE -> { files.sortedBy { it.extension } }
                }
                if (!isAscending) sortedFiles = sortedFiles.reversed()
                items(sortedFiles) { file ->
                    FileItem(file, context, fileViewModel, true, onFileSelected = {}, onFileDeselected = {})
                }
            }
        } else {
            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberTvLazyListState(refreshKey)
            ) {
                var sortedFiles : List<File>
                sortedFiles = when (sortType) {
                    SortType.NAME -> { files.sortedBy { it.name } }
                    SortType.DATE -> { files.sortedBy { it.lastModified() } }
                    SortType.SIZE -> { files.sortedBy { it.length() } }
                    SortType.TYPE -> { files.sortedBy { it.extension } }
                }
                if (!isAscending) sortedFiles = sortedFiles.reversed()
                items(sortedFiles) { file ->
                    FileItem(file, context, fileViewModel, isGridView, onFileSelected = {}, onFileDeselected = {})
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

    private fun getNewPath(pathComponents: List<String>, index: Int) = pathComponents.subList(0, index + 1).joinToString("/")

    @Composable
    fun CreateHomeDirButton(
        newPath: String,
        index: Int,
        pathComponents: List<String>
    ) {
        TextButton(
            onClick = { fileViewModel.loadStorage(File(newPath)) }
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home Directory",
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = if (index < pathComponents.size - 1) "Home >" else "Home",
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun CreateExternalStorageDirButton(
        newPath: String,
        fileViewModel: FileViewModel,
        index: Int,
        pathComponents: List<String>
    ) {
        TextButton(
            onClick = { fileViewModel.loadStorage(File(newPath)) }
        ) {
            Icon(
                imageVector = Icons.Default.Usb,
                contentDescription = "Storage Directory",
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = if (index != pathComponents.size - 1) {
                    "${fileViewModel.loadedExternalDevice} >"
                } else {
                    fileViewModel.loadedExternalDevice
                },
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    private fun CreateDefaultDirButton(
        newPath: String,
        component: String,
        index: Int,
        pathComponents: List<String>,
        fileViewModel: FileViewModel
    ) {
        TextButton(
            onClick = { fileViewModel.loadStorage(File(newPath)) }
        ) {
            Text(
                text = if (index != pathComponents.size - 1) "$component >" else component,
                fontWeight = FontWeight.Bold
            )
        }
    }
}