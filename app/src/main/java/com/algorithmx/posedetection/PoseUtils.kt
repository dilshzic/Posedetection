package com.algorithmx.posedetection

import com.algorithmx.posedetection.data.LandmarkData
import kotlin.math.atan2

object PoseUtils {
    fun getAngle(firstPoint: LandmarkData, midPoint: LandmarkData, lastPoint: LandmarkData): Double {
        var result = Math.toDegrees(
            (atan2(lastPoint.y - midPoint.y, lastPoint.x - midPoint.x) -
                    atan2(firstPoint.y - midPoint.y, firstPoint.x - midPoint.x)).toDouble()
        )
        result = Math.abs(result) // Angle should be positive
        if (result > 180) {
            result = 360.0 - result // So that the angle is the shorter of the two
        }
        return result
    }
}
