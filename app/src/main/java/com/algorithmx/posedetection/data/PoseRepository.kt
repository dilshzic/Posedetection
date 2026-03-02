package com.algorithmx.posedetection.data

import kotlinx.coroutines.flow.Flow

class PoseRepository(private val poseDao: PoseDao) {
    val allPoses: Flow<List<PoseEntity>> = poseDao.getAllPoses()
    val uniqueFolders: Flow<List<String>> = poseDao.getUniqueFolders()

    fun getPosesByFolder(folderName: String): Flow<List<PoseEntity>> {
        return poseDao.getPosesByFolder(folderName)
    }

    suspend fun getPosePathsInFolder(folderName: String): List<String> {
        return poseDao.getPosePathsInFolder(folderName)
    }

    suspend fun insertPose(pose: PoseEntity) {
        poseDao.insertPose(pose)
    }

    suspend fun updatePose(pose: PoseEntity) {
        poseDao.updatePose(pose)
    }

    suspend fun updateLabelsInBulk(ids: List<Long>, label: String) {
        poseDao.updateLabelsInBulk(ids, label)
    }

    suspend fun deletePoses(ids: List<Long>) {
        poseDao.deletePoses(ids)
    }

    suspend fun deleteFolder(folderName: String) {
        poseDao.deleteFolder(folderName)
    }

    suspend fun deleteAllPoses() {
        poseDao.deleteAll()
    }

    suspend fun getPoseById(id: Long): PoseEntity? {
        return poseDao.getPoseById(id)
    }
}
