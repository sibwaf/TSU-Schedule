package ru.dyatel.tsuschedule.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import org.jetbrains.anko.db.update
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class LessonDao(private val context: Context, databaseManager: DatabaseManager) : DatabasePart(databaseManager), EventListener {

    private object Columns {
        const val GROUP = "`group`"

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

        val TABLES = listOf(TABLE_UNFILTERED, TABLE_FILTERED)

        val LESSON_PARSER = object : MapRowParser<Pair<String, Lesson>> {
            override fun parseRow(columns: Map<String, Any?>): Pair<String, Lesson> {
                val group = columns[Columns.GROUP.removeSurrounding("`")] as String
                return group to Lesson(
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
        TABLES.forEach {
            db.createTable(it, true,
                    Columns.GROUP to TEXT,
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
            TABLES.forEach { db.dropTable(it, true) }
            createTables(db)
            return
        }
        if (oldVersion < 6) {
            val group = context.schedulePreferences.group
            val type = TEXT.render()
            for (table in TABLES) {
                db.execSQL("ALTER TABLE $table ADD COLUMN ${Columns.GROUP} $type")
                db.update(table, Columns.GROUP to group).exec()
            }
        }
    }

    fun update(group: String, lessons: Collection<Lesson>) {
        writableDatabase.transaction {
            delete(TABLE_UNFILTERED)
            lessons.map { it.toContentValues() }
                    .onEach { it.put(Columns.GROUP, group) }
                    .forEach { insert(TABLE_UNFILTERED, null, it) }
        }

        applyModifiers()
    }

    fun remove(group: String) {
        writableDatabase.transaction {
            val argument = arrayOf(group)
            TABLES.forEach { delete(it, "${Columns.GROUP} = ?", argument) }
        }
    }

    private fun applyModifiers() {
        val filters = databaseManager.filterDao.getFilters().filter { it.enabled }

        writableDatabase.transaction {
            delete(TABLE_FILTERED)
            select(TABLE_UNFILTERED).parseList(LESSON_PARSER)
                    .mapNotNull {
                        var result: Lesson? = it.second
                        for (filter in filters) {
                            if (result == null) break
                            result = filter.apply(result)
                        }
                        if (result != null) it.first to result else null
                    }
                    .map { it.second.toContentValues().apply { put(Columns.GROUP, it.first) } }
                    .forEach { insert(TABLE_FILTERED, null, it) }
        }

        EventBus.broadcast(Event.DATA_UPDATED)
    }

    fun getLessons(group: String): List<Lesson> = readableDatabase
            .select(TABLE_FILTERED)
            .whereSimple("${Columns.GROUP} = ?", group)
            .orderBy(Columns.TIME)
            .parseList(LESSON_PARSER)
            .map { it.second }

    override fun handleEvent(type: Event, payload: Any?) = applyModifiers()

}
