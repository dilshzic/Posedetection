package com.algorithmx.posedetection.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PoseDao {
    @Query("SELECT * FROM pose_results ORDER BY timestamp DESC")
    fun getAllPoses(): Flow<List<PoseEntity>>

    @Query("SELECT * FROM pose_results WHERE folderName = :folderName ORDER BY timestamp DESC")
    fun getPosesByFolder(folderName: String): Flow<List<PoseEntity>>

    @Query("SELECT DISTINCT folderName FROM pose_results")
    fun getUniqueFolders(): Flow<List<String>>

    @Query("SELECT imagePath FROM pose_results WHERE folderName = :folderName")
    suspend fun getPosePathsInFolder(folderName: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPose(pose: PoseEntity)

    @Update
    suspend fun updatePose(pose: PoseEntity)

    @Query("UPDATE pose_results SET label = :label WHERE id IN (:ids)")
    suspend fun updateLabelsInBulk(ids: List<Long>, label: String)

    @Query("DELETE FROM pose_results WHERE id IN (:ids)")
    suspend fun deletePoses(ids: List<Long>)

    @Query("DELETE FROM pose_results WHERE folderName = :folderName")
    suspend fun deleteFolder(folderName: String)

    @Query("SELECT * FROM pose_results WHERE id = :id")
    suspend fun getPoseById(id: Long): PoseEntity?

    @Query("DELETE FROM pose_results")
    suspend fun deleteAll()
}
