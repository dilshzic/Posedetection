package com.algorithmx.posedetection.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PoseTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromLandmarkList(value: List<LandmarkData>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLandmarkList(value: String): List<LandmarkData> {
        val listType = object : TypeToken<List<LandmarkData>>() {}.type
        return gson.fromJson(value, listType)
    }
}
