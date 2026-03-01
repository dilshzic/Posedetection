package com.algorithmx.posedetection.data

import com.google.mlkit.vision.pose.Pose

fun Pose.toLandmarkDataList(): List<LandmarkData> {
    return allPoseLandmarks.map { landmark ->
        LandmarkData(
            x = landmark.position3D.x,
            y = landmark.position3D.y,
            z = landmark.position3D.z,
            likelihood = landmark.inFrameLikelihood
        )
    }
}
