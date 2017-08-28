package ru.dyatel.tsuschedule.data

import android.content.Context
import hirondelle.date4j.DateTime
import ru.dyatel.tsuschedule.R
import java.util.TimeZone

enum class Parity(i: Int, private val textResource: Int) {

    ODD(0, R.string.odd_week), EVEN(1, R.string.even_week), BOTH(-1, 0);

    val index: Int = i
        get() {
            if (this == BOTH)
                throw IllegalArgumentException("Can't get index for Parity.BOTH!")
            return field
        }

    fun toText(context: Context): String {
        if (this == BOTH)
            throw IllegalArgumentException("Can't get text resource for Parity.BOTH!")
        return context.getString(textResource)
    }

}

fun indexToParity(index: Int) = when (index) {
    0 -> Parity.ODD
    1 -> Parity.EVEN
    else -> throw IllegalArgumentException("Bad index $index")
}

fun weekParityOf(date: DateTime): Parity {
    val academicYear = DateTime.forDateOnly(if (date.month < 9) date.year - 1 else date.year, 9, 1)

    // Sunday..Saturday -> Monday..Sunday
    val startWeekday = if (academicYear.weekDay == 1) 7 else academicYear.weekDay - 1

    val academicWeekStart = academicYear.minusDays(startWeekday - 1)
    return if (date.getWeekIndex(academicWeekStart) % 2 == 0) Parity.EVEN else Parity.ODD
}

val currentWeekParity
    get() = weekParityOf(DateTime.now(TimeZone.getDefault()))
