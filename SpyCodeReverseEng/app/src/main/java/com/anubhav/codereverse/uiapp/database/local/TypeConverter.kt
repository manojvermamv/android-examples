package com.anubhav.codereverse.uiapp.database.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.Serializable
import java.util.*

class TypeConverter : Serializable {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun fromDate(date: Gson?): String? {
        val gson = Gson()
        return gson.toJson(date)
    }

    @TypeConverter
    fun toDate(value: String?): Date? {
        val listType = object : TypeToken<Date?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return formatter.parse(value, OffsetDateTime::from)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromStringList(list: List<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String?>? {
        val listType = object : TypeToken<List<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

}

object OffsetDateTime {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @JvmStatic
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return formatter.parse(value, OffsetDateTime::from)
        }
    }

    @JvmStatic
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return date?.format(formatter)
    }

}