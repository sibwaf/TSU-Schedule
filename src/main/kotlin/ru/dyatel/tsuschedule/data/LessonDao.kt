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

private const val TABLE_UNFILTERED = "lessons"
private const val TABLE_FILTERED = "filtered"

private object LessonColumns {

    const val PARITY = "parity"
    const val WEEKDAY = "weekday"
    const val TIME = "time"

    const val DISCIPLINE = "discipline"
    const val AUDITORY = "auditory"
    const val TEACHER = "teacher"

    const val TYPE = "type"
    const val SUBGROUP = "subgroup"

}

class LessonDao(databaseManager: DatabaseManager) : DatabasePart(databaseManager), EventListener {

    init {
        EventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED)
    }

    override fun createTables(db: SQLiteDatabase) {
        listOf(TABLE_UNFILTERED, TABLE_FILTERED).forEach {
            db.createTable(it, true,
                    LessonColumns.PARITY to TEXT,
                    LessonColumns.WEEKDAY to TEXT,
                    LessonColumns.TIME to TEXT,
                    LessonColumns.DISCIPLINE to TEXT,
                    LessonColumns.AUDITORY to TEXT,
                    LessonColumns.TEACHER to TEXT,
                    LessonColumns.TYPE to TEXT,
                    LessonColumns.SUBGROUP to INTEGER
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

            select(TABLE_UNFILTERED).parseList(lessonParser)
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
            .orderBy(LessonColumns.TIME)
            .parseList(lessonParser)

    override fun handleEvent(type: Event, payload: Any?) = applyModifiers()

}

private fun Lesson.toContentValues(): ContentValues {
    val values = ContentValues()
    values.put(LessonColumns.PARITY, parity.toString())
    values.put(LessonColumns.WEEKDAY, weekday)
    values.put(LessonColumns.TIME, time)
    values.put(LessonColumns.DISCIPLINE, discipline)
    values.put(LessonColumns.AUDITORY, auditory)
    values.put(LessonColumns.TEACHER, teacher)
    values.put(LessonColumns.TYPE, type.toString())
    values.put(LessonColumns.SUBGROUP, subgroup)
    return values
}

private val lessonParser = object : MapRowParser<Lesson> {

    override fun parseRow(columns: Map<String, Any?>): Lesson {
        return Lesson(
                Parity.valueOf(columns[LessonColumns.PARITY] as String),
                columns[LessonColumns.WEEKDAY] as String,
                columns[LessonColumns.TIME] as String,
                columns[LessonColumns.DISCIPLINE] as String,
                columns[LessonColumns.AUDITORY] as String?,
                columns[LessonColumns.TEACHER] as String?,
                LessonType.valueOf(columns[LessonColumns.TYPE] as String),
                (columns[LessonColumns.SUBGROUP] as Long?)?.toInt()
        )
    }

}
