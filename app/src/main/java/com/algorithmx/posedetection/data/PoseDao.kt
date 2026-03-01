package com.algorithmx.posedetection.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPose(pose: PoseEntity): Long

    @Query("SELECT * FROM pose_results ORDER BY timestamp DESC")
    fun getAllPoses(): Flow<List<PoseEntity>>

    @Query("DELETE FROM pose_results")
    suspend fun deleteAll()

    @Query("SELECT * FROM pose_results WHERE id = :id")
    suspend fun getPoseById(id: Long): PoseEntity?
}
