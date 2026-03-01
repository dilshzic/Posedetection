package com.algorithmx.posedetection.logic

import android.content.Context
import android.net.Uri
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
            val image = InputImage.fromFilePath(context, imageUri)
            val pose = poseDetector.process(image).await()
            
            // ML Kit automatically handles EXIF rotation. 
            // We need to return the dimensions as seen by the detector after rotation.
            val isRotated = image.rotationDegrees == 90 || image.rotationDegrees == 270
            val width = if (isRotated) image.height else image.width
            val height = if (isRotated) image.width else image.height
            
            PoseResult(pose, width, height)
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
