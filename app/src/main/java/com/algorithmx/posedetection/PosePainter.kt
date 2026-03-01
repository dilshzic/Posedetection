package com.algorithmx.posedetection

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
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        fun drawLine(
            startLandmark: PoseLandmark?,
            endLandmark: PoseLandmark?,
            color: Color
        ) {
            if (startLandmark != null && endLandmark != null) {
                drawLine(
                    color = color,
                    start = Offset(startLandmark.position.x * scaleX, startLandmark.position.y * scaleY),
                    end = Offset(endLandmark.position.x * scaleX, endLandmark.position.y * scaleY),
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
                center = Offset(landmark.position.x * scaleX, landmark.position.y * scaleY)
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
