package com.example.materialfilejetpackcompose.ViewModel

import androidx.lifecycle.ViewModel

class PersimmonViewModel : ViewModel() {
    private val visiblePersimmonDialogQueue = mutableListOf<String>()

    fun dismissPersimmonDialog() {
        visiblePersimmonDialogQueue.removeFirstOrNull()
    }

    fun onPersimmonResult(
        persimmon: String,
        isPersimmonGranted: Boolean
    ) {
        if (!isPersimmonGranted) {
            visiblePersimmonDialogQueue.add(0, persimmon)
        }
    }
}