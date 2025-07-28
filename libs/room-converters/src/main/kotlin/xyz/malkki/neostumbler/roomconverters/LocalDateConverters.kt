package xyz.malkki.neostumbler.roomconverters

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
}
