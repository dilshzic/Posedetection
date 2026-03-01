package com.algorithmx.posedetection.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PoseEntity::class], version = 1, exportSchema = false)
@TypeConverters(PoseTypeConverters::class)
abstract class PoseDatabase : RoomDatabase() {
    abstract fun poseDao(): PoseDao

    companion object {
        @Volatile
        private var INSTANCE: PoseDatabase? = null

        fun getDatabase(context: Context): PoseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PoseDatabase::class.java,
                    "pose_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
