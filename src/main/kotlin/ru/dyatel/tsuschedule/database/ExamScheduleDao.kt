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
        const val DISCIPLINE = "discipline"
        const val CONSULTATION_DATETIME = "consultation_datetime"
        const val EXAM_DATETIME = "exam_datetime"
        const val CONSULTATION_AUDITORY = "consultation_auditory"
        const val EXAM_AUDITORY = "exam_auditory"
        const val TEACHER = "teacher"
    }

    private companion object {
        const val TABLE_EXAMS = "exams"

        val EXAM_PARSER = object : MapRowParser<Exam> {
            override fun parseRow(columns: Map<String, Any?>): Exam {
                return Exam(
                        columns[ExamColumns.DISCIPLINE] as String,
                        (columns[ExamColumns.CONSULTATION_DATETIME] as String?)?.let { DateTime(it) },
                        DateTime(columns[ExamColumns.EXAM_DATETIME] as String),
                        columns[ExamColumns.CONSULTATION_AUDITORY] as String?,
                        columns[ExamColumns.EXAM_AUDITORY] as String,
                        columns[ExamColumns.TEACHER] as String)
            }
        }
    }

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_EXAMS, true,
                ExamColumns.GROUP to TEXT,
                ExamColumns.DISCIPLINE to TEXT,
                ExamColumns.CONSULTATION_DATETIME to TEXT,
                ExamColumns.EXAM_DATETIME to TEXT,
                ExamColumns.CONSULTATION_AUDITORY to TEXT,
                ExamColumns.EXAM_AUDITORY to TEXT,
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
                    put(ExamColumns.DISCIPLINE, it.discipline)
                    put(ExamColumns.CONSULTATION_DATETIME, it.consultationDatetime?.toString())
                    put(ExamColumns.EXAM_DATETIME, it.examDatetime.toString())
                    put(ExamColumns.CONSULTATION_AUDITORY, it.consultationAuditory)
                    put(ExamColumns.EXAM_AUDITORY, it.examAuditory)
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
