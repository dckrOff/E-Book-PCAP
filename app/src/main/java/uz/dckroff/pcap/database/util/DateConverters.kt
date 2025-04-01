package uz.dckroff.pcap.database.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Конвертеры типов для Room (для работы с датами)
 */
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 