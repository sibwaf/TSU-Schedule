package ru.dyatel.tsuschedule.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
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

        val preferences = context.schedulePreferences

        if (oldVersion < 6) {
            val group = preferences.group
            if (group != null && group in preferences.groups) {
                val type = TEXT.render()
                for (table in TABLES) {
                    db.execSQL("ALTER TABLE $table ADD COLUMN ${Columns.GROUP} $type")
                    db.update(table, Columns.GROUP to group).exec()
                }
            } else {
                TABLES.forEach { db.dropTable(it, true) }
                createTables(db)
                return
            }
        }
    }

    fun update(group: String, lessons: Collection<Lesson>) {
        writableDatabase.transaction {
            remove(group)
            lessons.map { it.toContentValues() }
                    .onEach { it.put(Columns.GROUP, group) }
                    .forEach { insert(TABLE_UNFILTERED, null, it) }
        }

        applyModifiers(group)
    }

    fun remove(group: String) {
        writableDatabase.transaction {
            TABLES.forEach { delete(it, "${Columns.GROUP} = ?", arrayOf(group)) }
        }
    }

    private fun applyModifiers(group: String) {
        val filters = databaseManager.filters.request(group).filter { it.enabled }

        writableDatabase.transaction {
            delete(TABLE_FILTERED, "${Columns.GROUP} = ?", arrayOf(group))
            select(TABLE_UNFILTERED)
                    .whereSimple("${Columns.GROUP} = ?", group)
                    .parseList(LESSON_PARSER)
                    .mapNotNull {
                        var result: Lesson? = it
                        for (filter in filters) {
                            if (result == null)
                                break
                            result = filter.apply(result)
                        }
                        result
                    }
                    .map { it.toContentValues().apply { put(Columns.GROUP, group) } }
                    .forEach { insert(TABLE_FILTERED, null, it) }
        }

        EventBus.broadcast(Event.DATA_UPDATED)
    }

    fun request(group: String): List<Lesson> = readableDatabase
            .select(TABLE_FILTERED)
            .whereSimple("${Columns.GROUP} = ?", group)
            .orderBy(Columns.TIME)
            .parseList(LESSON_PARSER)

    override fun handleEvent(type: Event, payload: Any?) {
        context.schedulePreferences.groups.forEach { applyModifiers(it) }
    }

}
