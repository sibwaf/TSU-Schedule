package ru.dyatel.tsuschedule.data

import android.database.sqlite.SQLiteDatabase
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.parsing.Lesson
import ru.dyatel.tsuschedule.parsing.constructLessonFromCursor
import ru.dyatel.tsuschedule.parsing.getLessonColumnIndices
import ru.dyatel.tsuschedule.parsing.toContentValues
import ru.dyatel.tsuschedule.queryDV
import java.util.ArrayList

private const val TABLE_UNFILTERED = "lessons"
private const val TABLE_FILTERED = "filtered"

private const val WHERE_SUBGROUP = "${LessonTable.SUBGROUP}=0 OR ${LessonTable.SUBGROUP}=?"

class LessonDao(private val eventBus: EventBus,
                private val databaseManager: DatabaseManager) : DatabasePart, EventListener {

    init {
        eventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED)
    }

    override fun createTables(db: SQLiteDatabase) {
        db.execSQL(LessonTable.getCreateQuery(TABLE_UNFILTERED))
        db.execSQL(LessonTable.getCreateQuery(TABLE_FILTERED))
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(getDropTableQuery(TABLE_UNFILTERED))
        db.execSQL(getDropTableQuery(TABLE_FILTERED))
        createTables(db)
    }

    fun update(lessons: Collection<Lesson>) {
        val db = databaseManager.writableDatabase

        db.beginTransaction()
        try {
            db.delete(TABLE_UNFILTERED, null, null)
            lessons.forEach { db.insert(TABLE_UNFILTERED, null, it.toContentValues()) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        applyModifiers()
    }

    private fun applyModifiers() {
        val db = databaseManager.writableDatabase

        val cursor = db.queryDV(TABLE_UNFILTERED)
        val columnIndices = getLessonColumnIndices(cursor)

        db.beginTransaction()
        try {
            db.delete(TABLE_FILTERED, null, null)
            while (cursor.moveToNext()) {
                val lesson = constructLessonFromCursor(cursor, columnIndices)
                // TODO: apply filters
                db.insert(TABLE_FILTERED, null, lesson.toContentValues())
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            cursor.close()
        }

        eventBus.broadcast(Event.DATA_UPDATED)
    }

    fun request(subgroup: Int): List<Lesson> {
        val db = databaseManager.readableDatabase

        val cursor = if (subgroup != 0) db.queryDV(TABLE_FILTERED)
        else db.queryDV(TABLE_FILTERED, where = WHERE_SUBGROUP, whereArgs = arrayOf(subgroup.toString()))

        val indices = getLessonColumnIndices(cursor)

        val result = ArrayList<Lesson>()
        while (cursor.moveToNext()) result += constructLessonFromCursor(cursor, indices)

        cursor.close()

        return result
    }

    override fun handleEvent(type: Event) {
        applyModifiers()
    }

}
