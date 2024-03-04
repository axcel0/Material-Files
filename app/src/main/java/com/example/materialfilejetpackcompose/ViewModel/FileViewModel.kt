package com.example.materialfilejetpackcompose.ViewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.IOException
import java.util.Stack
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
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

    var filesToCopy = MutableLiveData<Set<File>?>(emptySet())
    var isCopying = false

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
        selectedFiles.value = emptySet()
    }

    fun getHomeDirectory(): File {
        return File("/storage/emulated/0")
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

    private fun clearSelectedFiles() {
        selectedFiles.value = emptySet()
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

    fun deleteFile(file: File) {
        Files.deleteIfExists(Paths.get(file.absolutePath))
        loadStorage(currentDirectory.value)
    }

    fun deleteFiles() {
        selectedFiles.value?.let { selectedFiles ->
            if (selectedFiles.isEmpty()) return

            selectedFiles.forEach { file ->
                try {
                    if (file.isDirectory) {
                        Files.walk(Paths.get(file.absolutePath))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach { it.delete() }
                    } else {
                        Files.delete(Paths.get(file.absolutePath))
                    }

                } catch (e: Exception) {
                    println("Failed to delete file: ${file.absolutePath}")
                    println("Reason: ${e.message}")
                    Toast.makeText(appContext, "Failed to delete file: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        loadStorage(currentDirectory.value)
        clearSelectedFiles()
    }

    private fun updateFilesInTrash() {
        filesInTrash = filesInTrash.distinct().sortedBy { it.name }.toMutableList()
    }

    fun createNewFolder(directory: String, folderName: String) {
        val newFolder = Paths.get(directory, folderName)
        if (Files.exists(newFolder)) {
            Toast.makeText(appContext, "Folder already exists", Toast.LENGTH_SHORT).show()
        } else {
            try {
                Files.createDirectory(newFolder)
                val mutableFiles = files.value?.toMutableList()
                mutableFiles?.add(newFolder.toFile())
                (files as MutableLiveData).value = mutableFiles
            } catch (e: IOException) {
                Toast.makeText(appContext, "Failed to create directory", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun cutFiles(selectedFiles: Set<File>) {
        filesToCopy.value = selectedFiles
        isCopying = false
    }

    fun copyFiles(selectedFiles: Set<File>) {
        filesToCopy.value = selectedFiles
        isCopying = true
    }

    fun pasteFiles(destinationDirectory: File) {
        filesToCopy.value?.forEach { file ->
            val isInSameDirectory = file.parent == destinationDirectory.absolutePath
            if (!isCopying && isInSameDirectory)
            {
                return
            }

            var newFile = File(destinationDirectory, file.name)
            var counter = 1
            while (newFile.exists()) {
                val fileName = file.nameWithoutExtension + "(${counter++})"
                val extension = file.extension
                newFile = if (extension.isNotEmpty()) {
                    File(destinationDirectory, "$fileName.$extension")
                } else {
                    File(destinationDirectory, fileName)
                }
            }

            if (file.isDirectory) {
                newFile.mkdirs()
                file.listFiles()?.forEach { childFile ->
                    filesToCopy.value = setOf(childFile)
                    pasteFiles(newFile)
                }
            } else {
                file.copyTo(newFile)
            }

            if (!isCopying) {
                deleteFile(file)
            }
        }

        loadStorage(destinationDirectory)
        filesToCopy.value = emptySet()
        clearSelectedFiles()
    }

    fun renameFile(oldFile: File, newName: String) {
        val newFile = File(oldFile.parent, newName)
        if (oldFile.renameTo(newFile)) {
            loadStorage(currentDirectory.value)
        } else {
            Toast.makeText(appContext, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFileInfo(file: File): String {
        val lastModified = file.lastModified()
        val size = file.length()
        val type = if (file.isDirectory) "Folder" else "File"
        return "Type: $type\nSize: $size bytes\nLast Modified: $lastModified"
    }

    fun restoreFiles() {
        filesInTrash.forEach { file ->
            file.copyTo(File(currentDirectory.value, file.name))
        }
        filesInTrash.clear()
    }


    //endregion File Operations
}