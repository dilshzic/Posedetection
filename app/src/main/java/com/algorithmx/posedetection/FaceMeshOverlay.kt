package com.algorithmx.posedetection

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
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        for (mesh in meshes) {
            // Draw all points
            for (point in mesh.allPoints) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 2f,
                    center = Offset(point.position.x * scaleX, point.position.y * scaleY)
                )
            }
        }
    }
}
