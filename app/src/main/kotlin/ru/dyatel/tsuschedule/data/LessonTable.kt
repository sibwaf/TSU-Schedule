package ru.dyatel.tsuschedule.data

import android.provider.BaseColumns
import android.provider.BaseColumns._ID
import org.jetbrains.anko.db.AUTOINCREMENT
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.PRIMARY_KEY
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.plus

object LessonTable : BaseColumns {

    const val PARITY = "parity"
    const val WEEKDAY = "weekday"
    const val TIME = "time"

    const val DISCIPLINE = "discipline"
    const val AUDITORY = "auditory"
    const val TEACHER = "teacher"

    const val TYPE = "type"
    const val SUBGROUP = "subgroup"

    val columns = arrayOf(
            _ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            PARITY to TEXT,
            WEEKDAY to TEXT,
            TIME to TEXT,
            DISCIPLINE to TEXT,
            AUDITORY to TEXT,
            TEACHER to TEXT,
            TYPE to TEXT,
            SUBGROUP to TEXT
    )

}
