package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.SqlType
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.select
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.LessonType
import ru.dyatel.tsuschedule.model.Parity

abstract class ScheduleDao<T : Lesson>(
        val table: String,
        protected val keyColumn: String,
        databaseManager: DatabaseManager
) : DatabasePart(databaseManager) {

    private object Columns {
        const val PARITY = "parity"
        const val WEEKDAY = "weekday"
        const val TIME = "time"

        const val DISCIPLINE = "discipline"
        const val AUDITORY = "auditory"

        const val TYPE = "type"
    }

    protected abstract class LessonParser<out T : Lesson> : MapRowParser<T> {
        override fun parseRow(columns: Map<String, Any?>): T {
            val base = Lesson(
                    Parity.valueOf(columns[Columns.PARITY] as String),
                    columns[Columns.WEEKDAY] as String,
                    columns[Columns.TIME] as String,
                    columns[Columns.DISCIPLINE] as String,
                    columns[Columns.AUDITORY] as String?,
                    LessonType.valueOf(columns[Columns.TYPE] as String)
            )
            return decorate(columns, base)
        }

        abstract fun decorate(columns: Map<String, Any?>, base: Lesson): T
    }

    protected abstract class LessonSerializer<in T : Lesson> {
        fun serialize(lesson: T): ContentValues {
            return ContentValues().apply {
                put(Columns.PARITY, lesson.parity.toString())
                put(Columns.WEEKDAY, lesson.weekday)
                put(Columns.TIME, lesson.time)
                put(Columns.DISCIPLINE, lesson.discipline)
                put(Columns.AUDITORY, lesson.auditory)
                put(Columns.TYPE, lesson.type.toString())

                decorate(lesson, this)
            }
        }

        abstract fun decorate(lesson: T, result: ContentValues)
    }

    protected abstract val parser: LessonParser<T>
    protected abstract val serializer: LessonSerializer<T>

    abstract fun decorateTable(columns: MutableMap<String, SqlType>)

    override fun createTables(db: SQLiteDatabase) {
        val columns = mutableMapOf(
                keyColumn to TEXT,
                Columns.PARITY to TEXT,
                Columns.WEEKDAY to TEXT,
                Columns.TIME to TEXT,
                Columns.DISCIPLINE to TEXT,
                Columns.AUDITORY to TEXT,
                Columns.TYPE to TEXT)
        decorateTable(columns)
        db.createTable(table, true, *columns.toList().toTypedArray())
    }

    fun request(key: String): List<T> {
        return execute {
            select(table)
                    .whereSimple("$keyColumn = ?", key)
                    .orderBy(Columns.TIME)
                    .parseList(parser)
        }
    }

    open fun save(key: String, lessons: Collection<T>) {
        executeTransaction {
            remove(key)
            lessons.forEach {
                val values = serializer.serialize(it).apply { put(keyColumn, key) }
                insert(table, null, values)
            }
        }
    }

    open fun remove(key: String) {
        execute {
            delete(table, "$keyColumn = ?", arrayOf(key))
        }
    }

}
