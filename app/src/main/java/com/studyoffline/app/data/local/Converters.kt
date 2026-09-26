package com.studyoffline.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyoffline.app.data.model.SessionType

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromSessionType(type: SessionType?): String {
        return type?.name ?: SessionType.FREE.name
    }

    @TypeConverter
    fun toSessionType(value: String?): SessionType {
        return try {
            SessionType.valueOf(value ?: SessionType.FREE.name)
        } catch (e: Exception) {
            SessionType.FREE
        }
    }
}
