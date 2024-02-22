package com.example.materialfilejetpackcompose.View

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.SortType
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ContentView(private val fileViewModel: FileViewModel) {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun Content() {
        val files by fileViewModel.files.observeAsState(emptyList())
        val context = LocalContext.current
        var isGridView by remember { mutableStateOf(false) }
        var sortOrder by remember { mutableStateOf("ascending") }
        var expanded by remember { mutableStateOf(false) }
        val currentDirectory by fileViewModel.currentDirectory.observeAsState()
        val selectedFiles = fileViewModel.selectedFiles.observeAsState()
        var sortType by remember { mutableStateOf(SortType.NAME) }
        var isAscending by remember { mutableStateOf(true) }
        var isFileOperationExpanded by remember { mutableStateOf(false) }

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                var isSortExpanded by remember { mutableStateOf(false) }
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
                                onClick = {
                                    isSortExpanded = false
                                    sortType = when (item) {
                                        "Sort by name" -> SortType.NAME
                                        "Sort by date" -> SortType.DATE
                                        "Sort by size" -> SortType.SIZE
                                        "Sort by type" -> SortType.TYPE
                                        else -> SortType.NAME
                                    }
                                },

                                ) {
                                Text(text = item)
                            }
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
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.padding(16.dp)
                ) {
                    when (sortType) {
                        SortType.NAME -> {
                            var sortedFiles = files!!.sortedBy { it.name }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                        SortType.DATE -> {
                            var sortedFiles = files!!.sortedBy { it.lastModified() }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                        SortType.SIZE -> {
                            var sortedFiles = files!!.sortedBy { it.length() }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                        SortType.TYPE -> {
                            var sortedFiles = files!!.sortedBy { it.extension }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (sortType) {
                        SortType.NAME -> {
                            var sortedFiles = files!!.sortedBy { it.name }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                        SortType.DATE -> {
                            var sortedFiles = files!!.sortedBy { it.lastModified() }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                        SortType.SIZE -> {
                            var sortedFiles = files!!.sortedBy { it.length() }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                        SortType.TYPE -> {
                            var sortedFiles = files!!.sortedBy { it.extension }
                            if (!isAscending) sortedFiles = sortedFiles.reversed()
                            items(sortedFiles) { file ->
                                FileItem(file, context, fileViewModel, isGridView)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun FileItem(file: File, context: Context, fileViewModel: FileViewModel, isGridView: Boolean) {
        var isSelected by remember { mutableStateOf(false) }
        val isDarkMode = isSystemInDarkTheme()

        ListItem(
            text = {
                if (isGridView) {
                    Text(file.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(file.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                if (file.isDirectory) {
                    if (isGridView) {
                        Icon(imageVector = Icons.Filled.Folder, contentDescription = "Folder", modifier = Modifier.size(56.dp), tint = Color(0xFFFFA400))
                    } else {
                        Icon(imageVector = Icons.Filled.Folder, contentDescription = "Folder", tint = Color(0xFFFFA400))
                    }
                } else {
                    if (isGridView) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "File", modifier = Modifier.size(72.dp), tint = Color(0xFF757575))
                    } else {
                        Icon(imageVector = Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "File", tint = Color(0xFF757575))
                    }
                }
            },
            modifier = Modifier
                .clickable {
                    if (file.isDirectory) {
                        fileViewModel.loadInternalStorage(file)
                    } else if (fileViewModel.isFilePhoto(file)) {
                        fileViewModel.openPhotoFile(file)
                    } else {
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.data = Uri.fromFile(file)
//                        context.startActivity(intent)
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
            trailing = {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        isSelected = it
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
}