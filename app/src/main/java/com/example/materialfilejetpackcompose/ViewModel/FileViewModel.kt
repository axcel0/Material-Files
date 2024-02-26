package com.example.materialfilejetpackcompose.ViewModel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.Stack
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale

enum class SortType {
    NAME, SIZE, DATE, TYPE
}

class FileViewModel(private val appContext: Context) : ViewModel() {

    private val _files: MutableLiveData<List<File>?> = MutableLiveData()
    val files: LiveData<List<File>?> = _files

    private val _currentDirectory: MutableLiveData<File> = MutableLiveData()
    val currentDirectory: LiveData<File> = _currentDirectory

    private val directoryStack = Stack<File>()

    private val _currentPath: MutableLiveData<String> = MutableLiveData("/")
    val currentPath: LiveData<String> = _currentPath

    var selectedFiles: MutableLiveData<Set<File>?> = MutableLiveData<Set<File>?>(emptySet())

    private var filesInTrash = mutableListOf<File>()
    private var filesToCopy = mutableListOf<File>()

    private var searchHistories = mutableListOf<String>()

    fun loadStorage(directory: File? = null) {
        val path = directory?.absolutePath
        directoryStack.push(directory)
        _currentPath.postValue(directoryStack.joinToString(separator = "/") { it.name })
        if (path != null) {
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                return
            }
        }
        if (directory != null) {
            val filteredFiles = directory.listFiles()?.toList()
            (files as MutableLiveData).postValue(filteredFiles)
        }
        (currentDirectory as MutableLiveData).postValue(directory)
    }

    //region Photos

    fun loadPhotosOnly(directory: File? = null) {
        val photoExtensions = listOf("jpg", "png", "jpeg", "gif")
        loadAllFilesWithExtensions(directory, photoExtensions)
    }

    fun isFilePhoto(file: File): Boolean {
        val photoExtensions = listOf("jpg", "png", "jpeg", "gif")
        return photoExtensions.contains(file.extension.lowercase(Locale.ROOT))
    }

    fun openMediaFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val mimeType: String = when {
            isFilePhoto(file) -> "image/*"
            isFileVideo(file) -> "video/*"
            isFileAudio(file) -> "audio/*"
            else -> "*/*"
        }
        intent.setDataAndType(Uri.parse(file.absolutePath), mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
    }

    //endregion Photos

    fun loadVideosOnly(directory: File? = null) {
        val videoExtensions = listOf("mp4", "avi", "flv", "mov")
        loadAllFilesWithExtensions(directory, videoExtensions)
    }
    fun isFileVideo(file: File): Boolean {
        val videoExtensions = listOf("mp4", "avi", "flv", "mov")
        return videoExtensions.contains(file.extension.lowercase(Locale.ROOT))
    }

    fun loadAudiosOnly(directory: File? = null) {
        val audioExtensions = listOf("mp3", "wav", "ogg", "flac")
        loadAllFilesWithExtensions(directory, audioExtensions)
    }

    fun isFileAudio(file: File): Boolean {
        val audioExtensions = listOf("mp3", "wav", "aac", "flac")
        return audioExtensions.contains(file.extension.lowercase(Locale.ROOT))
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

    //region Search

    private val _searchResults: MutableLiveData<List<File>> = MutableLiveData()
    val searchResults: LiveData<List<File>> = _searchResults

    fun searchFiles(query: String) {
        val currentDir = currentDirectory.value
        val results = currentDir?.listFiles { dir, name ->
            name.contains(query, ignoreCase = true)
        }?.toList() ?: emptyList()

        _searchResults.value = results
    }

    fun searchAllFiles(query: String) {

        val results = currentDirectory.value?.walk()?.filter { file ->
            file.name.contains(query, ignoreCase = true)
        }?.toList() ?: emptyList()

        _searchResults.value = results
    }

    //endregion Search

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

    private fun performFileOperation(operation: (Set<File>?) -> Unit, selectedFiles: Set<File>? = null) {
        operation(selectedFiles)
    }

    fun moveFilesToTrash(selectedFiles: Set<File>? = null) {
        performFileOperation({ files ->
            files?.let {
                filesInTrash.addAll(it)
                _files.value = _files.value?.minus(it)
            }
            updateFilesInTrash()
        }, selectedFiles)
    }

    fun moveFilesOutOfTrash(selectedFiles: Set<File>? = null) {
        performFileOperation({ files ->
            files?.let {
                filesInTrash.removeAll(it)
            }
            updateFilesInTrash()
        }, selectedFiles)
    }

    fun deleteFiles() {
        selectedFiles.value?.let { selectedFiles ->
            if (selectedFiles.isEmpty()) return

            _files.value = _files.value?.minus(selectedFiles)
            selectedFiles.forEach { file ->
                try {
                    Files.delete(Paths.get(file.absolutePath))
                } catch (e: Exception) {
                    println("Failed to delete file: ${file.absolutePath}")
                    println("Reason: ${e.message}")
                    Toast.makeText(appContext, "Failed to delete file: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFilesInTrash() {
        filesInTrash = filesInTrash.distinct().sortedBy { it.name }.toMutableList()
    }

    fun createNewFolder(directory: File, folderName: String) {
        val newFolder = File(directory, folderName)
        if (newFolder.exists()) {
            Toast.makeText(appContext, "Folder already exists", Toast.LENGTH_SHORT).show()
        } else {
            newFolder.mkdir()
            val mutableFiles = files.value?.toMutableList()
            mutableFiles?.add(newFolder)
            (files as MutableLiveData).value = mutableFiles
        }
    }

    fun copyFiles(selectedFiles: Set<File>) {
        filesToCopy.addAll(selectedFiles)
    }

    fun pasteFiles(destinationDirectory: File) {
        filesToCopy.forEach { file ->
            file.copyTo(File(destinationDirectory, file.name))
        }
        filesToCopy.clear()
    }

    fun renameFile(oldFile: File, newName: String) {
        val newFile = File(oldFile.parent, newName)
        val renamed = oldFile.renameTo(newFile)
        if (renamed) {
            val mutableFiles = files.value?.toMutableList()
            mutableFiles?.remove(oldFile)
            mutableFiles?.add(newFile)
            (files as MutableLiveData).value = mutableFiles
        } else {
            Toast.makeText(appContext, "Failed to rename file: ${oldFile.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreFiles() {
        filesInTrash.forEach { file ->
            file.copyTo(File(currentDirectory.value, file.name))
        }
        filesInTrash.clear()
    }


    //endregion File Operations
}