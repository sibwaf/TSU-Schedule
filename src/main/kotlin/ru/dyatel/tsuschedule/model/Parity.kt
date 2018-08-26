package ru.dyatel.tsuschedule.model

import android.content.Context
import hirondelle.date4j.DateTime
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.utilities.convertWeekday
import java.util.TimeZone

enum class Parity(private val textResource: Int) {

    ODD(R.string.odd_week), EVEN(R.string.even_week);

    fun toText(context: Context) = context.getString(textResource)!!

    fun stableHashcode(): Int = when (this) {
        ODD -> 2
        EVEN -> 4
    }

}

fun weekParityOf(date: DateTime): Parity {
    val academicYear = DateTime.forDateOnly(if (date.month < 9) date.year - 1 else date.year, 9, 1)

    val startWeekday = convertWeekday(academicYear.weekDay)

    val academicWeekStart = academicYear.minusDays(startWeekday - 1)
    return if (date.getWeekIndex(academicWeekStart) % 2 == 0) Parity.EVEN else Parity.ODD
}

val currentWeekParity
    get() = weekParityOf(DateTime.now(TimeZone.getDefault()))
