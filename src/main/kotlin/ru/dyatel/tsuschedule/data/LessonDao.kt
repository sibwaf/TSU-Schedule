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

class LessonDao(private val databaseManager: DatabaseManager) : DatabasePart, EventListener {

    init {
        EventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED)
    }

    private val readableDatabase
        get() = databaseManager.readableDatabase

    private val writableDatabase
        get() = databaseManager.writableDatabase

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
        db.dropTable(TABLE_UNFILTERED, true)
        db.dropTable(TABLE_FILTERED, true)

        createTables(db)
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
        writableDatabase.transaction {
            delete(TABLE_FILTERED)

            val filters = databaseManager.filterDao.getFilters()
            val subgroupFilter = databaseManager.filterDao.getSubgroupFilter()

            select(TABLE_UNFILTERED).parseList(lessonParser)
                    .mapNotNull {
                        var result: Lesson? = it
                        for (filter in filters) {
                            if (result == null) break
                            if (filter is ConsumingFilter)
                                result = filter.apply(result)
                        }
                        result
                    }
                    .mapNotNull { subgroupFilter.apply(it) }
                    .map { it.toContentValues() }
                    .forEach { insert(TABLE_FILTERED, null, it) }
        }

        EventBus.broadcast(Event.DATA_UPDATED)
    }

    fun getLessons(): List<Lesson> = readableDatabase
            .select(TABLE_FILTERED)
            .orderBy(Columns.TIME)
            .parseList(lessonParser)

    override fun handleEvent(type: Event, payload: Any?) = applyModifiers()

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

private val lessonParser = object : MapRowParser<Lesson> {

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
