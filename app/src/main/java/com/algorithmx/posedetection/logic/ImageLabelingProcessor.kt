package com.algorithmx.posedetection.logic

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

class ImageLabelingProcessor {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    suspend fun labelImage(context: Context, imageUri: Uri): List<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val labels = labeler.process(image).await()
            labels.map { "${it.text} (${(it.confidence * 100).toInt()}%)" }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
