package com.algorithmx.posedetection.data

import com.google.mlkit.vision.pose.Pose

fun Pose.toLandmarkDataList(): List<LandmarkData> {
    return allPoseLandmarks.map { landmark ->
        // Using position (2D) as fallback since position3D is unresolved in this version
        LandmarkData(
            x = landmark.position.x,
            y = landmark.position.y,
            z = 0f,
            likelihood = landmark.inFrameLikelihood
        )
    }
}
