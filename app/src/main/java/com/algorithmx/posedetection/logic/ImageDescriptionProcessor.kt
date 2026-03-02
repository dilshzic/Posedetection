package com.algorithmx.posedetection.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

// 1. Updated Common Imports
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.GenAiException

// 2. CRITICAL: Use Guava's await() for ListenableFuture, NOT tasks.await()
import kotlinx.coroutines.guava.await 

import com.google.mlkit.genai.imagedescription.ImageDescriber
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest

class ImageDescriptionProcessor(private val context: Context) {
    
    private val options = ImageDescriberOptions.builder(context).build()
    private val imageDescriber: ImageDescriber = ImageDescription.getClient(options)

    suspend fun describeImage(uri: Uri): String? {
        val bitmap = uriToBitmap(uri) ?: return null
        
        return try {
            // checkFeatureStatus returns ListenableFuture<Integer>
            val featureStatus = imageDescriber.checkFeatureStatus().await()
            
            when (featureStatus) {
                FeatureStatus.AVAILABLE -> {
                    startImageDescriptionRequest(bitmap)
                }
                FeatureStatus.DOWNLOADABLE -> {
                    // downloadFeature requires a callback, but returns a ListenableFuture<Void>
                    // which we can safely await to ensure the download finishes before continuing.
                    imageDescriber.downloadFeature(object : DownloadCallback {
                        override fun onDownloadStarted(bytesToDownload: Long) {}
                        override fun onDownloadProgress(totalBytesDownloaded: Long) {}
                        override fun onDownloadCompleted() {}
                        override fun onDownloadFailed(e: GenAiException) {}
                    }).await()
                    
                    startImageDescriptionRequest(bitmap)
                }
                FeatureStatus.DOWNLOADING -> {
                    "Model is currently downloading in the background. Please wait."
                }
                FeatureStatus.UNAVAILABLE -> {
                    "Model is unavailable on this device. (Check device support or AICore status)"
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun startImageDescriptionRequest(bitmap: Bitmap): String? {
        return try {
            val request = ImageDescriptionRequest.builder(bitmap).build()
            
            // runInference returns ListenableFuture<ImageDescriptionResult>
            val result = imageDescriber.runInference(request).await()
            
            // Because await() is now correctly resolved, the compiler knows this is
            // an ImageDescriptionResult, which has a .description property.
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
                @Suppress("DEPRECATION")
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
