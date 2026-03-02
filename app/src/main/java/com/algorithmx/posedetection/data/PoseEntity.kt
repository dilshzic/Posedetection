package com.algorithmx.posedetection.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

data class LandmarkData(
    val x: Float,
    val y: Float,
    val z: Float,
    val likelihood: Float
)

@Entity(
    tableName = "pose_results",
    indices = [Index(value = ["imagePath", "folderName"], unique = true)]
)
data class PoseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val folderName: String = "Default",
    val label: String? = null,
    val autoLabels: List<String> = emptyList(),
    val caption: String? = null,
    val timestamp: Long,
    val landmarks: List<LandmarkData>,
    val imageWidth: Int,
    val imageHeight: Int
)
