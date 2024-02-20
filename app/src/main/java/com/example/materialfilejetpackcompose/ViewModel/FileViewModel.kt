package com.example.materialfilejetpackcompose.ViewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.materialfilejetpackcompose.MainActivity
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
        val path = directory?.absolutePath
        directoryStack.push(directory)
        _currentPath.value = directoryStack.joinToString(separator = "/") { it.name }
        if (path != null) {
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                return
            }
        }
        if (directory != null) {
            (files as MutableLiveData).value = directory.listFiles()?.toList()
        }
        (currentDirectory as MutableLiveData).value = directory
    }

    fun loadPhotosOnly(directory: File? = null) {
        val photoExtensions = listOf("jpg", "png", "jpeg", "gif")
        loadFilesWithExtensions(directory, photoExtensions)
    }

    fun loadVideosOnly(directory: File? = null) {
        val videoExtensions = listOf("mp4", "avi", "flv", "mov")
        loadFilesWithExtensions(directory, videoExtensions)
    }

    fun loadAudiosOnly(directory: File? = null) {
        val audioExtensions = listOf("mp3", "wav", "ogg", "flac")
        loadFilesWithExtensions(directory, audioExtensions)
    }

    private fun loadFilesWithExtensions(directory: File? = null, extensions: List<String>) {
        val path = directory?.absolutePath
        directoryStack.push(directory)
        _currentPath.postValue(directoryStack.joinToString(separator = "/") { it.name })
        if (path != null) {
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                return
            }
        }
        if (directory != null) {
            val filteredFiles = directory.listFiles()?.filter { it.extension in extensions }
            (files as MutableLiveData).postValue(filteredFiles)
        }
        (currentDirectory as MutableLiveData).postValue(directory)
    }
    fun searchFiles(query: String) {
        val currentFiles = files.value ?: return
        val filteredFiles = currentFiles.filterTo(mutableListOf()) { it.name.contains(query, ignoreCase = true) }
        (files as MutableLiveData).value = filteredFiles
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