package com.example.materialfilejetpackcompose.View

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ImmersiveList
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.SortType
import kotlinx.coroutines.coroutineScope
import okhttp3.internal.notify
import okhttp3.internal.notifyAll
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
                        val isHomeDir = pathComponents.getOrNull(index + 2) == "0"
                        if (isHomeDir) {
                            TextButton(
                                onClick = {
                                    val newPath = pathComponents.subList(0, index + 3).joinToString("/")
                                    fileViewModel.loadStorage(File(newPath))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home Directory",
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = if (index < pathComponents.size - 3) "Home >" else "Home",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (index > 3) {
                            TextButton(
                                onClick = {
                                    val newPath = pathComponents.subList(0, index + 1).joinToString("/")
                                    fileViewModel.loadStorage(File(newPath))
                                }
                            ) {
                                Text(
                                    text = if (index != pathComponents.size - 1) "$component >" else component,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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

    @OptIn(ExperimentalFoundationApi::class, ExperimentalTvMaterial3Api::class)
    @Composable
    fun FileItem(file: File, context: Context, fileViewModel: FileViewModel, isGridView: Boolean) {
        val selectedFiles by fileViewModel.selectedFiles.collectAsState(emptySet())
        var isSelected = selectedFiles!!.contains(file)
        val isDarkMode = isSystemInDarkTheme()
        val maxChars = if (isGridView) 16 else 32
        val displayName = if (file.name.length > maxChars) {
            file.name.substring(0, maxChars) + "..."
        } else {
            file.name
        }
        val refreshKey by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedFiles) {
            isSelected = selectedFiles!!.contains(file)
            refreshKey.inc()
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
                when {
                    file.isDirectory -> {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Folder",
                            modifier = if (isGridView) Modifier.size(56.dp) else Modifier.size(24.dp),
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
            },
            modifier = Modifier
                .clickable {
                    if (selectedFiles.isNullOrEmpty()) {
                        if (file.isDirectory) {
                            fileViewModel.loadStorage(file)
                        } else if (fileViewModel.isFilePhoto(file) || fileViewModel.isFileAudio(file) || fileViewModel.isFileVideo(file)) {
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
                }
                .background(
                    if (!isSelected)
                        Color.Transparent
                    else if (isDarkMode)
                        Color(0x33FFFFFF)
                    else
                        Color(0x80000000)
                )
                .padding(8.dp),
            trailingContent = {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        if (it) {
                            fileViewModel.addSelectedFile(file)
                        } else {
                            fileViewModel.removeSelectedFile(file)
                        }
                    }
                )
            }
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(16.dp),
                state = rememberLazyGridState(),
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
                    FileItem(file, context, fileViewModel, isGridView)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState(refreshKey)
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
                    FileItem(file, context, fileViewModel, isGridView)
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
}