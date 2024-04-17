package com.example.materialfilejetpackcompose.ViewModel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Stack
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

enum class SortType {
    NAME, SIZE, DATE, TYPE
}

class FileViewModel(private val appContext: Context) : ViewModel() {

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _files: MutableLiveData<List<File>?> = MutableLiveData()
    val files: LiveData<List<File>?> = _files

    private val _currentDirectory: MutableLiveData<File> = MutableLiveData()
    val currentDirectory: LiveData<File> = _currentDirectory

    var directoryStack = Stack<File>()

    private val _currentPath: MutableLiveData<String> = MutableLiveData("/")
    val currentPath: LiveData<String> = _currentPath

    var selectedFiles: MutableStateFlow<Set<File>?> = MutableStateFlow(emptySet())

    private var filesInTrash = mutableListOf<File>()

    var filesToCopy = MutableLiveData<Set<File>?>(emptySet())
    var isCopying = false

    var searchHistories = MutableLiveData<List<String>>()

    val externalDevices = MutableLiveData<List<StorageVolume>>()
    private val externalStorageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_MEDIA_MOUNTED, Intent.ACTION_MEDIA_UNMOUNTED -> {
                    // Refresh the list of external devices
                    externalDevices.value = getExternalStorageDevices()
                }
            }
        }
    }

    var loadedExternalDevice = String()

    init {
        // Register the broadcast receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            // Note: The data scheme must be set to "file" for these intents
            addDataScheme("file")
        }
        appContext.registerReceiver(externalStorageReceiver, filter)
    }

    fun cleanup() {
        appContext.unregisterReceiver(externalStorageReceiver)
    }

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

    fun getExternalStorageDevices(): List<StorageVolume> {
        val storageManager = appContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return storageManager.storageVolumes.filter { it.isRemovable }
    }

    fun isExternalStorage(): Boolean {
        return currentDirectory.value?.absolutePath?.contains("/storage/emulated/0") == false
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
        if (file.exists() && !file.isDirectory) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                intent.setDataAndType(Uri.parse(file.absolutePath), mimeType ?: "*/*")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                appContext.startActivity(intent)
            } catch (e: Exception) {
                // Log the exception or show a user-friendly error message
                println("Failed to open media file: ${e.message}")
            }
        } else {
            // Log the error or show a user-friendly error message
            println("File does not exist or is a directory")
        }
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
        val results = mutableListOf<File>()

        currentDir?.walk()?.forEach { file ->
            if (file.name.contains(query, ignoreCase = true)) {
                results.add(file)
            }
        }

        _searchResults.value = results
    }

    //endregion Search

    fun addSelectedFile(file: File) {
        val mutableSelectedFiles = selectedFiles.value?.toMutableSet() ?: mutableSetOf()
        mutableSelectedFiles.add(file)
        selectedFiles.value = mutableSelectedFiles
    }

    fun removeSelectedFile(file: File) {
        val mutableSelectedFiles = selectedFiles.value?.toMutableSet()
        mutableSelectedFiles?.remove(file)
        selectedFiles.value = mutableSelectedFiles
    }

    fun updateSelectedFiles(files: Set<File>) {
        viewModelScope.launch {
            selectedFiles.value = files
        }
    }

    private fun clearSelectedFiles() {
        selectedFiles.value = emptySet()
    }

    //region File Operations
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

    fun restoreFiles() {
        filesInTrash.forEach { file ->
            file.copyTo(File(currentDirectory.value, file.name))
        }
        filesInTrash.clear()
    }
    private fun updateFilesInTrash() {
        filesInTrash = filesInTrash.distinct().sortedBy { it.name }.toMutableList()
    }
    private fun performFileOperation(operation: (Set<File>?) -> Unit, selectedFiles: Set<File>? = null) {
        operation(selectedFiles)
    }

    private fun deleteFile(file: File) {
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
        // Check if the directory is empty
        if (destinationDirectory.listFiles()?.isEmpty() == true) {
            // Copy the empty directory to the home directory
            destinationDirectory.copyTo(getHomeDirectory())
            return
        }

        filesToCopy.value?.forEach { file ->
            val isInSameDirectory = file.parent == destinationDirectory.absolutePath
            if (!isCopying && isInSameDirectory) {
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
    private fun getTotalFiles(files: Set<File>): Int {
        var total = 0
        for (file in files) {
            if (file.isDirectory) {
                total += file.walk().count()
            } else {
                total++
            }
        }
        return total
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
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val lastModified = sdf.format(Date(file.lastModified()))
        val size: Long = if (file.isDirectory) {
            file.walk().filter { it.isFile }.map { it.length() }.sum()
        } else {
            file.length()
        }
        val readableSize = getReadableFileSize(size)
        val type = when {
            file.isDirectory -> "Folder"
            isFileVideo(file) -> "Video"
            isFileAudio(file) -> "Audio"
            isFilePhoto(file) -> "Photo"
            else -> "File"
        }
        val isReadable = if (file.canRead()) "Yes" else "No"
        val isWritable = if (file.canWrite()) "Yes" else "No"
        val isHidden = if (file.isHidden) "Yes" else "No"
        val content = if (file.isDirectory) {
            val itemCount = file.list()?.size ?: 0
            "$itemCount ${if (itemCount <= 1) "item" else "items"}"
        } else {
            "Not applicable"
        }

        return "Type: $type\nSize: $readableSize\nLast Modified: $lastModified\nReadable: $isReadable\nWritable: $isWritable\nHidden: $isHidden\nContains: $content"
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun cancelOperation() {
        filesToCopy.value = emptySet()
        isCopying = false
        clearSelectedFiles()
    }


    //endregion File Operations
}