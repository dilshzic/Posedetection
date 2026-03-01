package com.algorithmx.posedetection.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.imagedescription.ImageDescription
import com.google.mlkit.vision.imagedescription.ImageDescriptionRequest
import com.google.mlkit.vision.imagedescription.ImageDescriberOptions
import kotlinx.coroutines.tasks.await

class ImageDescriptionProcessor(private val context: Context) {
    private val options = ImageDescriberOptions.builder(context).build()
    private val imageDescriber = ImageDescription.getClient(options)

    suspend fun describeImage(uri: Uri): String? {
        return try {
            val bitmap = uriToBitmap(uri) ?: return null
            
            // Following the documentation for GenAI Image Description
            val request = ImageDescriptionRequest.builder(bitmap).build()
            val result = imageDescriber.runInference(request).await()
            result.description
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        imageDescriber.close()
    }
}
