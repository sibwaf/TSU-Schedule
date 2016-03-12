package ru.dyatel.tsuschedule.parsing

import android.content.ContentValues
import ru.dyatel.tsuschedule.data.LessonTable

fun Lesson.toContentValues(): ContentValues {
    val values = ContentValues()
    values.put(LessonTable.PARITY, parity.toString())
    values.put(LessonTable.WEEKDAY, weekday)
    values.put(LessonTable.TIME, time)
    values.put(LessonTable.DISCIPLINE, discipline)
    values.put(LessonTable.AUDITORY, auditory)
    values.put(LessonTable.TEACHER, teacher)
    values.put(LessonTable.TYPE, type.toString())
    values.put(LessonTable.SUBGROUP, subgroup)
    return values
}
