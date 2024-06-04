package com.example.materialfilejetpackcompose.ViewModel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack
import kotlin.math.log10
import kotlin.math.pow

enum class SortType {
    NAME, SIZE, DATE, TYPE
}

class FileViewModel(private val appContext: Context) : ViewModel() {
    val pasteProgress: MutableLiveData<Int> = MutableLiveData(0)

    private val _files: MutableLiveData<List<File>?> = MutableLiveData()
    val files: LiveData<List<File>?> = _files

    private val _currentDirectory: MutableLiveData<File> = MutableLiveData()
    val currentDirectory: LiveData<File> = _currentDirectory

    var directoryStack = Stack<File>()

    private val _currentPath: MutableLiveData<String> = MutableLiveData("/")
    val currentPath: LiveData<String> = _currentPath

    var selectedFiles: MutableStateFlow<Set<File>?> = MutableStateFlow(emptySet())

    private var filesInTrash = mutableListOf<File>()

    internal var filesToCopy = MutableLiveData<Set<File>?>(emptySet())
    private var isCopying = false

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

    fun isAndroidTV(): Boolean {
        return appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    fun isPortrait(): Boolean {
        return appContext.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
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

    fun loadPhotosOnly(directory: File? = null) {
        loadAllFilesWithExtensions(directory) { file ->
            isFilePhoto(file)
        }
    }

    fun isFilePhoto(file: File): Boolean {
        val extension = file.extension.lowercase(Locale.ROOT)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType?.startsWith("image/") == true
    }

    fun loadVideosOnly(directory: File? = null) {
        loadAllFilesWithExtensions(directory) { file ->
            isFileVideo(file)
        }
    }

    fun isFileVideo(file: File): Boolean {
        val extension = file.extension.lowercase(Locale.ROOT)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType?.startsWith("video/") == true
    }

    fun loadAudiosOnly(directory: File? = null) {
        loadAllFilesWithExtensions(directory) { file ->
            isFileAudio(file)
        }
    }

    fun isFileAudio(file: File): Boolean {
        val extension = file.extension.lowercase(Locale.ROOT)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType?.startsWith("audio/") == true
    }

    private fun loadAllFilesWithExtensions(directory: File? = null, filter: (File) -> Boolean) {
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
                if (file.isFile && filter(file)) {
                    filteredFiles.add(file)
                }
            }
            (files as MutableLiveData).postValue(filteredFiles)
        }
        (currentDirectory as MutableLiveData).postValue(directory)
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

    //region Search

    private val _searchResults: MutableLiveData<List<File>> = MutableLiveData()
    val searchResults: LiveData<List<File>> = _searchResults

    internal val searchLoadProgress = MutableLiveData(0)

    fun searchFiles(query: String) {
        val currentDir = currentDirectory.value
        val results = mutableListOf<File>()

        searchLoadProgress.value = 1 // 1 = show progress bar
        currentDir?.walk()?.forEach { file ->
            if (file.name.contains(query, ignoreCase = true)) {
                results.add(file)
            }
        }
        searchLoadProgress.value = 0 // 0 = hide progress bar

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
        clearSelectedFiles()
    }

    fun copyFiles(selectedFiles: Set<File>) {
        filesToCopy.value = selectedFiles
        isCopying = true
        clearSelectedFiles()
    }
    fun pasteFiles(destination: File) {
        val files = filesToCopy.value ?: return
        val totalFiles = files.size
        var copiedFiles = 0
        var progress: Int
        pasteProgress.value = 0

        val onFinished = {
            loadStorage(currentDirectory.value)
            filesToCopy.value = emptySet()
            isCopying = false
            clearSelectedFiles()
            viewModelScope.launch {
                resetPasteProgress()
            }
        }

        files.forEach { file ->
            var newFile = File(destination, file.name)
            var counter = 1
            while (newFile.exists()) {
                val fileName = file.nameWithoutExtension + "(${counter++})"
                val extension = file.extension
                newFile = if (extension.isNotEmpty()) {
                    File(destination, "$fileName.$extension")
                } else {
                    File(destination, fileName)
                }
            }

            try {
                if (isCopying) {
                    file.copyTo(newFile, overwrite = true)
                } else {
                    file.copyTo(newFile, overwrite = true)
                    file.delete()
                }

                copiedFiles++
                progress = (copiedFiles.toFloat() / totalFiles.toFloat() * 100).toInt()
                pasteProgress.value = progress

                val isFinished = copiedFiles == totalFiles
                if (isFinished) {
                    onFinished()
                }
            } catch (e: Exception) {
                println("Failed to paste file: ${file.absolutePath}")
                println("Reason: ${e.message}")
                Toast.makeText(appContext, "Failed to paste file: ${file.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun resetPasteProgress() {
        withContext(Dispatchers.IO) {
            Thread.sleep(2000)
        }
        pasteProgress.postValue(0)
    }

    fun renameFile(oldFile: File, newName: String): String {
        val oldFileName = oldFile.name

        if (oldFile.isDirectory) {
            val newFile = File(oldFile.parent, newName)
            try {
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                loadStorage(currentDirectory.value)
            } catch (e: Exception) {
                Toast.makeText(appContext, "Failed to rename directory", Toast.LENGTH_SHORT).show()
            }
        } else {
            val nameParts = newName.split(".")
            val newExtension = if (nameParts.size > 1) nameParts.last() else oldFile.extension
            val newFileName = if (nameParts.size > 1) nameParts.dropLast(1).joinToString(".") else newName
            val newFile = File(oldFile.parent, "$newFileName.$newExtension")
            try {
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                loadStorage(currentDirectory.value)
            } catch (e: Exception) {
                Toast.makeText(appContext, "Fortunately Success to rename file", Toast.LENGTH_SHORT).show()
            }
        }
        return oldFileName
    }

    fun getFileInfo(file: File): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val lastModified = sdf.format(Date(file.lastModified()))
        val size = getFileSize(file)
        val readableSize = getReadableFileSize(size)
        val format = file.extension
        val type = getFileType(file)
        val isReadable = if (file.canRead()) "Yes" else "No"
        val isWritable = if (file.canWrite()) "Yes" else "No"
        val isHidden = if (file.isHidden) "Yes" else "No"
        val content = if (file.isDirectory) {
            val itemCount = file.list()?.size ?: 0
            "$itemCount ${if (itemCount <= 1) "item" else "items"}"
        } else {
            "Not applicable"
        }
        val codec = getCodec(file, format, type)

        return """
        Type: $type
        Format: $format
        Size: $readableSize
        Last Modified: $lastModified
        Readable: $isReadable
        Writable: $isWritable
        Hidden: $isHidden
        Contains: $content
        Codec: $codec
    """.trimIndent()
    }

    private fun getFileSize(file: File): Long {
        return if (file.isDirectory) {
            Files.walk(file.toPath()).filter { it.toFile().isFile }.mapToLong { it.toFile().length() }.sum()
        } else {
            file.length()
        }
    }

    private fun getFileType(file: File): String {
        return when {
            file.isDirectory -> "Folder"
            isFileVideo(file) -> "Video"
            isFileAudio(file) -> "Audio"
            isFilePhoto(file) -> "Photo"
            else -> "File"
        }
    }

    private fun getCodec(file: File, format: String, type: String): String {
        return when (type) {
            "Video", "Audio" -> getMediaCodec(file)
            "Photo" -> format
            else -> "Not applicable"
        }
    }

    private fun getMediaCodec(file: File): String {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(file.absolutePath)
            val format = extractor.getTrackFormat(0)
            when (val codec = format.getString(MediaFormat.KEY_MIME)?.substringAfter("/")) {
                "avc" -> "H.264/AVC"
                "hevc" -> "H.265/HEVC"
                else -> codec ?: "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        } finally {
            extractor.release()
        }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
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