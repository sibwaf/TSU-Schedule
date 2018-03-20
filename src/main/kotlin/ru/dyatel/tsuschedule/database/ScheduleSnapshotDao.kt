package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.DEFAULT
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createIndex
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update
import ru.dyatel.tsuschedule.model.RawSchedule
import ru.dyatel.tsuschedule.model.ScheduleSnapshot
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class ScheduleSnapshotDao(context: Context, databaseManager: DatabaseManager) : DatabasePart(databaseManager) {

    private object Columns {
        const val TIMESTAMP = "timestamp"
        const val GROUP = "`group`"
        const val DATA = "data"
        const val HASH = "hash"
        const val PINNED = "pinned"
        const val SELECTED = "selected"
    }

    private companion object {
        const val TABLE = "lessons_raw"

        val ROW_PARSER = object : MapRowParser<ScheduleSnapshot> {
            override fun parseRow(columns: Map<String, Any?>): ScheduleSnapshot {
                val schedule = RawSchedule(columns[Columns.TIMESTAMP] as Long, columns[Columns.DATA] as String)
                val pinned = (columns[Columns.PINNED] as Long).asFlag()
                val selected = (columns[Columns.SELECTED] as Long).asFlag()
                return ScheduleSnapshot(schedule, pinned, selected)
            }
        }

        fun Boolean.toInt() = if (this) 1 else 0
        fun Long.asFlag() = this != 0L
    }

    private val preferences = context.schedulePreferences

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE, true,
                Columns.TIMESTAMP to INTEGER,
                Columns.GROUP to TEXT,
                Columns.DATA to TEXT,
                Columns.HASH to INTEGER,
                Columns.PINNED to INTEGER + DEFAULT("0"),
                Columns.SELECTED to INTEGER + DEFAULT("1"))

        db.createIndex("timestamp_group", TABLE, true, true,
                Columns.TIMESTAMP, Columns.GROUP)
        db.createIndex("group_data", TABLE, true, true,
                Columns.GROUP, Columns.DATA)
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.dropTable(TABLE)
            createTables(db)
            return
        }
    }

    fun request(group: String, hash: Int? = null): List<ScheduleSnapshot> {
        return execute {
            val query = select(TABLE).orderBy(Columns.TIMESTAMP)

            if (hash != null) {
                query.whereSimple("${Columns.GROUP} = ? AND ${Columns.HASH} = ?", group, hash.toString())
            } else {
                query.whereSimple("${Columns.GROUP} = ?", group)
            }

            query.parseList(ROW_PARSER)
        }
    }

    fun save(group: String, raw: RawSchedule, hash: Int) {
        executeTransaction {
            update(TABLE, Columns.SELECTED to false.toInt())
                    .whereSimple("${Columns.GROUP} = ?", group)
                    .exec()

            val contentValues = ContentValues().apply {
                put(Columns.GROUP, group)
                put(Columns.TIMESTAMP, raw.timestamp)
                put(Columns.DATA, raw.data)
                put(Columns.HASH, hash)
            }
            insertWithOnConflict(TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)

            execSQL("DELETE FROM $TABLE " +
                    "WHERE ${Columns.TIMESTAMP} IN (" +
                    "SELECT ${Columns.TIMESTAMP} FROM $TABLE " +
                    "WHERE ${Columns.GROUP} = ? AND ${Columns.PINNED} = 0 " +
                    "ORDER BY ${Columns.TIMESTAMP} DESC " +
                    "LIMIT -1 OFFSET ?" +
                    ")", arrayOf(group, preferences.historySize))
        }
    }

    fun update(group: String, raw: RawSchedule, pinned: Boolean, selected: Boolean) {
        execute {
            update(TABLE, Columns.PINNED to pinned.toInt(), Columns.SELECTED to selected.toInt())
                    .whereSimple("${Columns.GROUP} = ? AND ${Columns.TIMESTAMP} = ?",
                            group, raw.timestamp.toString())
                    .exec()
        }
    }

    fun remove(group: String, raw: RawSchedule) {
        execute {
            delete(TABLE, "${Columns.GROUP} = ? AND ${Columns.TIMESTAMP} = ?",
                    arrayOf(group, raw.timestamp.toString()))
        }
    }

}
