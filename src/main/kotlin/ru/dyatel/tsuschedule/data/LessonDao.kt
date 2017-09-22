package ru.dyatel.tsuschedule.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener

class LessonDao(databaseManager: DatabaseManager) : DatabasePart(databaseManager), EventListener {

    private object Columns {
        const val PARITY = "parity"
        const val WEEKDAY = "weekday"
        const val TIME = "time"

        const val DISCIPLINE = "discipline"
        const val AUDITORY = "auditory"
        const val TEACHER = "teacher"

        const val TYPE = "type"
        const val SUBGROUP = "subgroup"
    }

    private companion object {

        const val TABLE_UNFILTERED = "lessons"
        const val TABLE_FILTERED = "filtered"

        val LESSON_PARSER = object : MapRowParser<Lesson> {
            override fun parseRow(columns: Map<String, Any?>): Lesson {
                return Lesson(
                        Parity.valueOf(columns[Columns.PARITY] as String),
                        columns[Columns.WEEKDAY] as String,
                        columns[Columns.TIME] as String,
                        columns[Columns.DISCIPLINE] as String,
                        columns[Columns.AUDITORY] as String?,
                        columns[Columns.TEACHER] as String?,
                        LessonType.valueOf(columns[Columns.TYPE] as String),
                        (columns[Columns.SUBGROUP] as Long?)?.toInt()
                )
            }
        }

    }

    private fun Lesson.toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(Columns.PARITY, parity.toString())
        values.put(Columns.WEEKDAY, weekday)
        values.put(Columns.TIME, time)
        values.put(Columns.DISCIPLINE, discipline)
        values.put(Columns.AUDITORY, auditory)
        values.put(Columns.TEACHER, teacher)
        values.put(Columns.TYPE, type.toString())
        values.put(Columns.SUBGROUP, subgroup)
        return values
    }

    init {
        EventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED)
    }

    override fun createTables(db: SQLiteDatabase) {
        listOf(TABLE_UNFILTERED, TABLE_FILTERED).forEach {
            db.createTable(it, true,
                    Columns.PARITY to TEXT,
                    Columns.WEEKDAY to TEXT,
                    Columns.TIME to TEXT,
                    Columns.DISCIPLINE to TEXT,
                    Columns.AUDITORY to TEXT,
                    Columns.TEACHER to TEXT,
                    Columns.TYPE to TEXT,
                    Columns.SUBGROUP to INTEGER
            )
        }
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.dropTable(TABLE_UNFILTERED, true)
            db.dropTable(TABLE_FILTERED, true)
            createTables(db)
        }

        applyModifiers()
    }

    fun update(lessons: Collection<Lesson>) {
        writableDatabase.transaction {
            delete(TABLE_UNFILTERED)
            lessons.map { it.toContentValues() }
                    .forEach { insert(TABLE_UNFILTERED, null, it) }
        }

        applyModifiers()
    }

    private fun applyModifiers() {
        val filters = databaseManager.filterDao.getFilters().filter { it.enabled }

        writableDatabase.transaction {
            delete(TABLE_FILTERED)

            select(TABLE_UNFILTERED).parseList(LESSON_PARSER)
                    .mapNotNull {
                        var result: Lesson? = it
                        for (filter in filters) {
                            if (result == null) break
                            result = filter.apply(result)
                        }
                        result
                    }
                    .map { it.toContentValues() }
                    .forEach { insert(TABLE_FILTERED, null, it) }
        }

        EventBus.broadcast(Event.DATA_UPDATED)
    }

    fun getLessons(): List<Lesson> = readableDatabase
            .select(TABLE_FILTERED)
            .orderBy(Columns.TIME)
            .parseList(LESSON_PARSER)

    override fun handleEvent(type: Event, payload: Any?) = applyModifiers()

}
