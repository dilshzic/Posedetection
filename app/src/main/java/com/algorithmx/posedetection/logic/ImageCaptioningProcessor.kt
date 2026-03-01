package com.algorithmx.posedetection.logic

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.caption.ImageCaptioning
import com.google.mlkit.vision.caption.ImageCaptionerOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

class ImageCaptioningProcessor {
    private val options = ImageCaptionerOptions.Builder()
        .setMaxConfidenceCaptions(1)
        .build()
    private val captioner = ImageCaptioning.getClient(options)

    suspend fun captionImage(context: Context, imageUri: Uri): String? {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = captioner.process(image).await()
            result.firstOrNull()?.text
        } catch (e: Exception) {
            null
        }
    }
}
