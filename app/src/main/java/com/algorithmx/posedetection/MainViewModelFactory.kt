package com.algorithmx.posedetection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.algorithmx.posedetection.data.PoseRepository

class MainViewModelFactory(private val repository: PoseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
