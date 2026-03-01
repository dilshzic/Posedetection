package com.algorithmx.posedetection.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.mlkit.vision.facemesh.FaceMesh

@Composable
fun FaceMeshOverlay(
    meshes: List<FaceMesh>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        if (imageWidth <= 0 || imageHeight <= 0) return@Canvas

        val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val canvasAspectRatio = canvasWidth / canvasHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if (imageAspectRatio > canvasAspectRatio) {
            // Image is wider than canvas -> letterboxed top/bottom
            scale = canvasWidth / imageWidth
            offsetX = 0f
            offsetY = (canvasHeight - imageHeight * scale) / 2f
        } else {
            // Image is taller than canvas -> letterboxed sides
            scale = canvasHeight / imageHeight
            offsetY = 0f
            offsetX = (canvasWidth - imageWidth * scale) / 2f
        }

        for (mesh in meshes) {
            for (point in mesh.allPoints) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 2f,
                    center = Offset(
                        point.position.x * scale + offsetX,
                        point.position.y * scale + offsetY
                    )
                )
            }
        }
    }
}
