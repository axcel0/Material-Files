package com.example.materialfilejetpackcompose.ViewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.Stack

enum class SortType {
    NAME, SIZE, DATE, TYPE
}

class FileViewModel(private val appContext: Context) : ViewModel() {

    var files: LiveData<List<File>?> = MutableLiveData()
    val currentDirectory: LiveData<File> = MutableLiveData()
    val directoryStack = Stack<File>()
    private val _currentPath: MutableLiveData<String> = MutableLiveData("/")
    val currentPath: LiveData<String> = _currentPath
    var selectedFiles: MutableLiveData<Set<File>?> = MutableLiveData<Set<File>?>(emptySet())
    var filesInTrash = mutableListOf<File>()
    var filesToCopy = mutableListOf<File>()

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

    //region Photos

    fun loadPhotosOnly(directory: File? = null) {
        val photoExtensions = listOf("jpg", "png", "jpeg", "gif")
        loadAllFilesWithExtensions(directory, photoExtensions)
    }

    fun isFilePhoto(file: File): Boolean {
        val photoExtensions = listOf("jpg", "png", "jpeg", "gif")
        return file.extension in photoExtensions
    }

    fun openPhotoFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(file.absolutePath), "image/*")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
    }

    //endregion Photos

    fun loadVideosOnly(directory: File? = null) {
        val videoExtensions = listOf("mp4", "avi", "flv", "mov")
        loadAllFilesWithExtensions(directory, videoExtensions)
    }

    fun loadAudiosOnly(directory: File? = null) {
        val audioExtensions = listOf("mp3", "wav", "ogg", "flac")
        loadAllFilesWithExtensions(directory, audioExtensions)
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

    private fun loadAllFilesWithExtensions(directory: File? = null, extensions: List<String>) {
        val path = directory?.absolutePath
        directoryStack.push(directory)
        _currentPath.postValue(directoryStack.joinToString(separator = "/") { it.name })
        if (path != null) {
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                return
            }
        }
        if (directory != null) {
            val filteredFiles = mutableListOf<File>()
            directory.walk().forEach { file ->
                if (file.isFile && file.extension in extensions) {
                    filteredFiles.add(file)
                }
            }
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
        val mutableSelectedFiles = selectedFiles.value?.toMutableSet()
        mutableSelectedFiles?.add(file)
        selectedFiles.value = mutableSelectedFiles
    }

    fun removeSelectedFile(file: File) {
        val mutableSelectedFiles = selectedFiles.value?.toMutableSet()
        mutableSelectedFiles?.remove(file)
        selectedFiles.value = mutableSelectedFiles
    }

    //region File Operations

    fun moveFilesToTrash(selectedFiles: Set<File>? = null) {
        if (selectedFiles == null) return

        filesInTrash.addAll(selectedFiles)
        filesInTrash = filesInTrash.distinct().toMutableList()
        filesInTrash.sortBy { it.name }

        files.value?.toMutableSet()?.removeAll(selectedFiles)
    }

    fun moveFilesOutOfTrash(selectedFiles: Set<File>? = null) {
        if (selectedFiles == null) return

        filesInTrash.removeAll(selectedFiles)
        filesInTrash = filesInTrash.distinct().toMutableList()
        filesInTrash.sortBy { it.name }
    }

    fun deleteFiles() {
        val mutableSelectedFiles = selectedFiles.value?.toMutableSet()
        val mutableFiles = files.value?.toMutableSet()

        if (mutableSelectedFiles!!.isEmpty()) return

        mutableFiles?.removeAll(mutableSelectedFiles)
        mutableSelectedFiles.forEach { it.delete() }

        (files as MutableLiveData).value = mutableFiles?.toMutableList()
        selectedFiles.value = mutableSelectedFiles
    }

    fun copyFiles(selectedFiles: Set<File>? = null) {
        if (selectedFiles == null) return

        filesToCopy.addAll(selectedFiles)
        filesToCopy = filesToCopy.distinct().toMutableList()
        filesToCopy.sortBy { it.name }
    }

    fun pasteFiles(destinationDirectory: File) {
        filesToCopy.forEach { file ->
            file.copyTo(File(destinationDirectory, file.name))
        }
        filesToCopy.clear()
    }

    //endregion File Operations
}