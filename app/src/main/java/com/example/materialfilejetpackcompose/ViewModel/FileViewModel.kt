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
    val currentDirectory: LiveData<File> = MutableLiveData()
    val directoryStack = Stack<File>()
    private val _currentPath: MutableLiveData<String> = MutableLiveData("/")
    val currentPath: LiveData<String> = _currentPath
    // This set will hold all selected files
    private val _selectedFiles = MutableLiveData<Set<File>?>(emptySet())
    val selectedFiles: MutableLiveData<Set<File>?> = _selectedFiles

    fun loadInternalStorage(directory: File? = null) {
        if (directory != null) {
            val path = directory.absolutePath
            directoryStack.push(directory)
            _currentPath.value = directoryStack.joinToString(separator = "/") { it.name }
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                return
            }
            (files as MutableLiveData).value = directory.listFiles()?.toList()
            (currentDirectory as MutableLiveData).value = directory
        } else {
            if (directoryStack.isNotEmpty()) {
                val parentDirectory = directoryStack.pop()
                if (parentDirectory != null) {
                    _currentPath.value = directoryStack.joinToString(separator = "/") { it.name }
                    loadInternalStorage(parentDirectory)
                } else {
                    _currentPath.value = "/"
                }
            }
        }
    }

    fun searchFiles(fileList: LiveData<List<File>>, query: String): LiveData<List<File>> {
        val searchResults = fileList.value?.filter { it.name.contains(query, ignoreCase = true) }
        (files as MutableLiveData).value = searchResults
        return files
    }

    fun addSelectedFile(file: File) {
        val selectedFiles = _selectedFiles.value?.toMutableSet() ?: mutableSetOf()
        selectedFiles.add(file)
        _selectedFiles.value = selectedFiles
    }

    fun removeSelectedFile(file: File) {
        val selectedFiles = _selectedFiles.value?.toMutableSet() ?: mutableSetOf()
        selectedFiles.remove(file)
        _selectedFiles.value = selectedFiles
    }

}