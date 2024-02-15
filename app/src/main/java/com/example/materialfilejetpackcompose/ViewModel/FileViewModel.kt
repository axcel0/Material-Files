package com.example.materialfilejetpackcompose.ViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.Stack

enum class SortType {
    NAME, SIZE, DATE, TYPE
}

class FileViewModel(private val appContext: Context) : ViewModel() {
    val files: LiveData<List<File>> = MutableLiveData()
    val currentDirectory: LiveData<File?> = MutableLiveData()
    val directoryStack = Stack<File>()

    fun getFileList(directory: File? = null): LiveData<List<File>> {
        if (directory != null) {
            val path = directory.absolutePath
            directoryStack.add(directory)
            (currentDirectory as MutableLiveData).value = directory
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                return files
            }
            (files as MutableLiveData).value = directory.listFiles()?.toList()
            currentDirectory.value = directory
        } else {
            if (directoryStack.isNotEmpty()) {
                val parentDirectory = directoryStack.removeAt(directoryStack.size - 1)
                if (parentDirectory != null) {
                    (currentDirectory as MutableLiveData).value = parentDirectory
                    return getFileList(parentDirectory)
                } else {
                    (currentDirectory as MutableLiveData).value = File("/")
                }
            }
        }
        return files
    }

    fun getSortedFileList(fileList: LiveData<List<File>>, isAscending: Boolean, sortType: SortType): LiveData<List<File>> {
        val sortedList = fileList.value?.toMutableList()
        when (sortType) {
            SortType.NAME -> {
                sortedList?.sortBy { it.name }
            }
            SortType.SIZE -> {
                sortedList?.sortBy { it.length() }
            }
            SortType.DATE -> {
                sortedList?.sortBy { it.lastModified() }
            }
            SortType.TYPE -> {
                sortedList?.sortBy { it.extension }
            }
        }
        if (!isAscending) {
            sortedList?.reverse()
        }
        (files as MutableLiveData).value = sortedList
        return files
    }

    fun searchFiles(fileList: LiveData<List<File>>, query: String): LiveData<List<File>> {
        val searchResults = fileList.value?.filter { it.name.contains(query, ignoreCase = true) }
        (files as MutableLiveData).value = searchResults
        return files
    }

}