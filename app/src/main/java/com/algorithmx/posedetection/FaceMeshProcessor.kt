package com.algorithmx.posedetection

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
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
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val image = InputImage.fromBitmap(bitmap, 0)
            val meshes = detector.process(image).await()
            FaceMeshResult(meshes, bitmap.width, bitmap.height)
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
