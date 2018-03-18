package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.SqlType
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.dropTable
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.TeacherLesson

class TeacherScheduleDao(
        databaseManager: DatabaseManager
) : ScheduleDao<TeacherLesson>("lessons_teacher", "teacher", databaseManager) {

    private object Columns {
        const val GROUPS = "groups"
    }

    private companion object {
        val PARSER = object : LessonParser<TeacherLesson>() {
            override fun decorate(columns: Map<String, Any?>, base: Lesson): TeacherLesson {
                return with(base) {
                    val groups = (columns[Columns.GROUPS] as String).split("\n")
                    TeacherLesson(parity, weekday, time, discipline, auditory, type, groups)
                }
            }
        }

        val SERIALIZER = object : LessonSerializer<TeacherLesson>() {
            override fun decorate(lesson: TeacherLesson, result: ContentValues) {
                result.apply {
                    put(Columns.GROUPS, lesson.groups.joinToString("\n"))
                }
            }
        }
    }

    override val parser = PARSER
    override val serializer = SERIALIZER

    override fun decorateTable(columns: MutableMap<String, SqlType>) {
        columns[Columns.GROUPS] = TEXT
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.dropTable(table)
            createTables(db)
            return
        }
    }

}
