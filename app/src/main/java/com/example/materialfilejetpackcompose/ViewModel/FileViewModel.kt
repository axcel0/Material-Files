package com.example.materialfilejetpackcompose.ViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

enum class SortType {
    NAME, SIZE, DATE, TYPE
}

class FileViewModel(private val appContext: Context) : ViewModel() {
    val files: LiveData<List<File>> = MutableLiveData()
    val currentDirectory: LiveData<File> = MutableLiveData()
    val directoryStack = mutableListOf<File>()

    fun getFileList(directory: File? = null): LiveData<List<File>> {
        if (directory != null) {
            directoryStack.add(directory)
            (currentDirectory as MutableLiveData).value = directory
            (files as MutableLiveData).value = directory.listFiles()?.toList()
        } else {
            if (directoryStack.isNotEmpty()) {
                val parentDirectory = directoryStack.removeAt(directoryStack.size - 1)
                (currentDirectory as MutableLiveData).value = parentDirectory
                (files as MutableLiveData).value = parentDirectory.listFiles()?.toList()
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


}