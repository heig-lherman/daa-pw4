package ch.heigvd.iict.daa.labo4.converter

import androidx.room.TypeConverter
import java.util.Calendar
import java.util.Date

/**
 * Defines type converters for the [Calendar] class.
 *
 * @author Emilie Bressoud
 * @author Loïc Herman
 * @author Sacha Butty
 */
class CalendarConverter {

    /**
     * Converts a long to a calendar.
     */
    @TypeConverter
    fun toCalendar(dateLong: Long): Calendar = Calendar.getInstance().apply {
        time = Date(dateLong)
    }

    /**
     * Converts a calendar to a long.
     */
    @TypeConverter
    fun fromCalendar(calendar: Calendar): Long = calendar.time.time
}