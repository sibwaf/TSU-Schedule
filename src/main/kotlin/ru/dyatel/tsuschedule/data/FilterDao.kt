package ru.dyatel.tsuschedule.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class FilterDao(private val databaseManager: DatabaseManager, private val context: Context) : DatabasePart {

    override fun createTables(db: SQLiteDatabase) {
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun getFilters(): List<Filter> = emptyList()

    fun getSubgroupFilter() = object : ConsumingFilter() {
        override fun apply(lesson: Lesson): Lesson? {
            if (lesson.subgroup == null) return lesson

            val subgroup = context.schedulePreferences.subgroup
            if (subgroup == 0) return lesson
            if (lesson.subgroup == subgroup) return lesson

            return null
        }
    }

}
