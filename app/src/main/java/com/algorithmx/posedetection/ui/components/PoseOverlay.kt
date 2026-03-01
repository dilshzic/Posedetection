package com.algorithmx.posedetection.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

@Composable
fun PoseOverlay(
    pose: Pose,
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

        fun drawLine(
            startLandmark: PoseLandmark?,
            endLandmark: PoseLandmark?,
            color: Color
        ) {
            if (startLandmark != null && endLandmark != null) {
                drawLine(
                    color = color,
                    start = Offset(
                        startLandmark.position.x * scale + offsetX,
                        startLandmark.position.y * scale + offsetY
                    ),
                    end = Offset(
                        endLandmark.position.x * scale + offsetX,
                        endLandmark.position.y * scale + offsetY
                    ),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
        }

        val landmarks = pose.allPoseLandmarks
        for (landmark in landmarks) {
            drawCircle(
                color = Color.Red,
                radius = 10f,
                center = Offset(
                    landmark.position.x * scale + offsetX,
                    landmark.position.y * scale + offsetY
                )
            )
        }

        // Draw connections
        val white = Color.White
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER), pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER), pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW), pose.getPoseLandmark(PoseLandmark.LEFT_WRIST), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER), pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW), pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER), pose.getPoseLandmark(PoseLandmark.LEFT_HIP), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER), pose.getPoseLandmark(PoseLandmark.RIGHT_HIP), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_HIP), pose.getPoseLandmark(PoseLandmark.RIGHT_HIP), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_HIP), pose.getPoseLandmark(PoseLandmark.LEFT_KNEE), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_KNEE), pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_HIP), pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE), white)
        drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE), pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE), white)
    }
}
