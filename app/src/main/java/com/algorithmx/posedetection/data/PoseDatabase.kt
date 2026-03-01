package com.algorithmx.posedetection.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PoseEntity::class], version = 1, exportSchema = false)
@TypeConverters(PoseTypeConverters::class)
abstract class PoseDatabase : RoomDatabase() {
    abstract fun poseDao(): PoseDao
}
