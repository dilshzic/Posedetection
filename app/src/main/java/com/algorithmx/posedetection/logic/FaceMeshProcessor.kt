package com.algorithmx.posedetection.logic

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import kotlinx.coroutines.tasks.await

class FaceMeshProcessor {
    private val options = FaceMeshDetectorOptions.Builder()
        .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
        .build()

    private val detector: FaceMeshDetector = FaceMeshDetection.getClient(options)

    suspend fun detectFaceMesh(context: Context, imageUri: Uri): FaceMeshResult? {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val meshes = detector.process(image).await()
            
            val isRotated = image.rotationDegrees == 90 || image.rotationDegrees == 270
            val width = if (isRotated) image.height else image.width
            val height = if (isRotated) image.width else image.height
            
            FaceMeshResult(meshes, width, height)
        } catch (e: Exception) {
            null
        }
    }

    data class FaceMeshResult(
        val meshes: List<FaceMesh>,
        val imageWidth: Int,
        val imageHeight: Int
    )
}
