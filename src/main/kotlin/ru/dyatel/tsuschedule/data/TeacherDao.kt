package ru.dyatel.tsuschedule.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.UNIQUE
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select

class TeacherDao(databaseManager: DatabaseManager) : DatabasePart(databaseManager) {

    private object TeacherColumns {
        const val ID = "id"
        const val NAME = "name"
    }

    private companion object {
        const val TABLE_TEACHERS = "teachers"

        val TEACHER_PARSER = object : MapRowParser<Teacher> {
            override fun parseRow(columns: Map<String, Any?>): Teacher {
                return Teacher(
                        columns[TeacherColumns.ID] as String,
                        columns[TeacherColumns.NAME] as String)
            }
        }
    }

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_TEACHERS, true,
                TeacherColumns.ID to TEXT + UNIQUE,
                TeacherColumns.NAME to TEXT)
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.dropTable(TABLE_TEACHERS, true)
            createTables(db)
            return
        }
    }

    fun request(query: String? = null): List<Teacher> {
        return execute {
            val teachers = select(TABLE_TEACHERS)
                    .orderBy(TeacherColumns.NAME)
                    .parseList(TEACHER_PARSER)

            if (query != null) {
                teachers.filter { it.name.contains(query, true) }
            } else {
                teachers
            }
        }
    }

    fun save(teachers: Collection<Teacher>) {
        executeTransaction {
            teachers.forEach {
                val contentValues = ContentValues()
                contentValues.put(TeacherColumns.ID, it.id)
                contentValues.put(TeacherColumns.NAME, it.name)
                insertWithOnConflict(TABLE_TEACHERS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    fun remove(id: String) {
        execute {
            delete(TABLE_TEACHERS, "${TeacherColumns.ID} = ?", arrayOf(id))
        }
    }

}
