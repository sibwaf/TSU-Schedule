package ru.dyatel.tsuschedule.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.parsing.Lesson
import ru.dyatel.tsuschedule.parsing.Parity

private const val TABLE_UNFILTERED = "lessons"
private const val TABLE_FILTERED = "filtered"

class LessonDao(private val eventBus: EventBus,
                private val databaseManager: DatabaseManager) : DatabasePart, EventListener {

    init {
        eventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED)
    }

    override fun createTables(db: SQLiteDatabase) {
        db.use {
            it.createTable(TABLE_UNFILTERED, columns = *LessonTable.columns)
            it.createTable(TABLE_FILTERED, columns = *LessonTable.columns)
        }
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.use {
            it.dropTable(TABLE_UNFILTERED, true)
            it.dropTable(TABLE_FILTERED, true)
        }
        createTables(db)
    }

    fun update(lessons: Collection<Lesson>) {
        databaseManager.use {
            transaction {
                delete(TABLE_UNFILTERED)
                lessons.forEach {
                    insert(TABLE_UNFILTERED, null, it.toContentValues())
                }
            }
        }

        applyModifiers()
    }

    private fun applyModifiers() {
        databaseManager.use {
            transaction {
                delete(TABLE_FILTERED)

                val lessons = select(TABLE_UNFILTERED).parseList(lessonParser)
                // TODO: apply filters
                lessons.forEach { insert(TABLE_FILTERED, null, it.toContentValues()) }
            }
        }

        eventBus.broadcast(Event.DATA_UPDATED)
    }

    fun request(subgroup: Int): List<Lesson> = databaseManager.use {
        val select = select(TABLE_FILTERED).orderBy(LessonTable.TIME)

        if (subgroup != 0) select.where("${LessonTable.SUBGROUP}=0 OR ${LessonTable.SUBGROUP}={subgroup}",
                "subgroup" to subgroup)

        select.parseList(lessonParser)
    }

    override fun handleEvent(type: Event) {
        applyModifiers()
    }

}

private fun Lesson.toContentValues(): ContentValues {
    val values = ContentValues()
    values.put(LessonTable.PARITY, parity.toString())
    values.put(LessonTable.WEEKDAY, weekday)
    values.put(LessonTable.TIME, time)
    values.put(LessonTable.DISCIPLINE, discipline)
    values.put(LessonTable.AUDITORY, auditory)
    values.put(LessonTable.TEACHER, teacher)
    values.put(LessonTable.TYPE, type.toString())
    values.put(LessonTable.SUBGROUP, subgroup)
    return values
}

private val lessonParser = object : MapRowParser<Lesson> {

    override fun parseRow(columns: Map<String, Any?>): Lesson {
        return Lesson(
                Parity.valueOf(columns[LessonTable.PARITY] as String),
                columns[LessonTable.WEEKDAY] as String,
                columns[LessonTable.TIME] as String,
                columns[LessonTable.DISCIPLINE] as String,
                columns[LessonTable.AUDITORY] as String,
                columns[LessonTable.TEACHER] as String,
                Lesson.Type.valueOf(columns[LessonTable.TYPE] as String),
                (columns[LessonTable.SUBGROUP] as String).toInt()
        )
    }

}
