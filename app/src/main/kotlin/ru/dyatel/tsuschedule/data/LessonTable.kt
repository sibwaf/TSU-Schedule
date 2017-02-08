package ru.dyatel.tsuschedule.data

import android.provider.BaseColumns
import android.provider.BaseColumns._ID

object LessonTable : BaseColumns {

    const val PARITY = "parity"
    const val WEEKDAY = "weekday"
    const val TIME = "time"

    const val DISCIPLINE = "discipline"
    const val AUDITORY = "auditory"
    const val TEACHER = "teacher"

    const val TYPE = "type"
    const val SUBGROUP = "subgroup"

    fun getCreateQuery(name: String) = "CREATE TABLE $name (" +
            "$_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "$PARITY TEXT," +
            "$WEEKDAY TEXT," +
            "$TIME TEXT," +
            "$DISCIPLINE TEXT," +
            "$AUDITORY TEXT," +
            "$TEACHER TEXT," +
            "$TYPE TEXT," +
            "$SUBGROUP CHAR(1)" +
            ")"

}
