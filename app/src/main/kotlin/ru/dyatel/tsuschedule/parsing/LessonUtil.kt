package ru.dyatel.tsuschedule.parsing

import android.content.ContentValues
import android.database.Cursor
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

private fun getColumnPairFromCursor(cursor: Cursor, column: String): Pair<String, Int> =
        column to cursor.getColumnIndexOrThrow(column)

fun getLessonColumnIndices(cursor: Cursor): Map<String, Int> =
        mapOf(
                getColumnPairFromCursor(cursor, LessonTable.PARITY),
                getColumnPairFromCursor(cursor, LessonTable.WEEKDAY),
                getColumnPairFromCursor(cursor, LessonTable.TIME),

                getColumnPairFromCursor(cursor, LessonTable.DISCIPLINE),
                getColumnPairFromCursor(cursor, LessonTable.AUDITORY),
                getColumnPairFromCursor(cursor, LessonTable.TEACHER),

                getColumnPairFromCursor(cursor, LessonTable.TYPE),
                getColumnPairFromCursor(cursor, LessonTable.SUBGROUP)
        )

fun constructLessonFromCursor(cursor: Cursor, columnIndices: Map<String, Int>) =
        Lesson(
                Parity.valueOf(cursor.getString(columnIndices[LessonTable.PARITY]!!)),
                cursor.getString(columnIndices[LessonTable.WEEKDAY]!!),
                cursor.getString(columnIndices[LessonTable.TIME]!!),

                cursor.getString(columnIndices[LessonTable.DISCIPLINE]!!),
                cursor.getString(columnIndices[LessonTable.AUDITORY]!!),
                cursor.getString(columnIndices[LessonTable.TEACHER]!!),

                Lesson.Type.valueOf(cursor.getString(columnIndices[LessonTable.TYPE]!!)),
                cursor.getInt(columnIndices[LessonTable.SUBGROUP]!!)
        )
