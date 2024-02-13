package com.example.materialfilejetpackcompose.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.materialfilejetpackcompose.ViewModel.FileViewModel
import com.example.materialfilejetpackcompose.ViewModel.SortType

class ContentView(private val fileViewModel: FileViewModel) {

    @Composable
    fun Content() {
        var isGridView by remember { mutableStateOf(false) }
        var sortType by remember { mutableStateOf(SortType.NAME) }
        var isAscending by remember { mutableStateOf(true) }
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
        }
    }

}