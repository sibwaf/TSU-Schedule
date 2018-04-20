package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import hirondelle.date4j.DateTime
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import ru.dyatel.tsuschedule.model.Exam

class ExamScheduleDao(databaseManager: DatabaseManager) : DatabasePart(databaseManager) {

    private object ExamColumns {
        const val GROUP = "group_index"
        const val DATETIME = "exam_datetime"
        const val DISCIPLINE = "discipline"
        const val AUDITORY = "auditory"
        const val TEACHER = "teacher"
    }

    private companion object {
        const val TABLE_EXAMS = "exams"

        val EXAM_PARSER = object : MapRowParser<Exam> {
            override fun parseRow(columns: Map<String, Any?>): Exam {
                return Exam(
                        DateTime(columns[ExamColumns.DATETIME] as String),
                        columns[ExamColumns.DISCIPLINE] as String,
                        columns[ExamColumns.AUDITORY] as String,
                        columns[ExamColumns.TEACHER] as String)
            }
        }
    }

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_EXAMS, true,
                ExamColumns.GROUP to TEXT,
                ExamColumns.DATETIME to TEXT,
                ExamColumns.DISCIPLINE to TEXT,
                ExamColumns.AUDITORY to TEXT,
                ExamColumns.TEACHER to TEXT)
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.dropTable(TABLE_EXAMS, true)
            createTables(db)
            return
        }
    }

    fun request(group: String): List<Exam> {
        return execute {
            select(TABLE_EXAMS)
                    .whereSimple("${ExamColumns.GROUP} = ?", group)
                    .parseList(EXAM_PARSER)
                    .sorted()
        }
    }

    fun save(group: String, exams: Collection<Exam>) {
        executeTransaction {
            remove(group)

            exams.forEach {
                val contentValues = ContentValues().apply {
                    put(ExamColumns.GROUP, group)
                    put(ExamColumns.DATETIME, it.datetime.toString())
                    put(ExamColumns.DISCIPLINE, it.discipline)
                    put(ExamColumns.AUDITORY, it.auditory)
                    put(ExamColumns.TEACHER, it.teacher)
                }

                insert(TABLE_EXAMS, null, contentValues)
            }
        }
    }

    fun remove(group: String) {
        execute {
            delete(TABLE_EXAMS, "${ExamColumns.GROUP} = ?", arrayOf(group))
        }
    }

}
