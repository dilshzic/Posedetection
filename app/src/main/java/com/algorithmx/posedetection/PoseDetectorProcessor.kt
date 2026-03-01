package com.algorithmx.posedetection

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.tasks.await

class PoseDetectorProcessor {
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    suspend fun detectPose(context: Context, imageUri: Uri): PoseResult? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val image = InputImage.fromBitmap(bitmap, 0)
            val pose = poseDetector.process(image).await()
            PoseResult(pose, bitmap.width, bitmap.height)
        } catch (e: Exception) {
            null
        }
    }

    data class PoseResult(
        val pose: Pose,
        val imageWidth: Int,
        val imageHeight: Int
    )
}
