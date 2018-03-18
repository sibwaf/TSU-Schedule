package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.PRIMARY_KEY
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.UNIQUE
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import ru.dyatel.tsuschedule.model.RawSchedule
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class ScheduleSnapshotDao(context: Context, databaseManager: DatabaseManager) : DatabasePart(databaseManager) {

    private object Columns {
        const val TIMESTAMP = "timestamp"
        const val GROUP = "`group`"
        const val DATA = "data"
        const val HASH = "hash"
    }

    private companion object {
        const val TABLE_RAW_SCHEDULE = "lessons_raw"

        val ROW_PARSER = object : MapRowParser<RawSchedule> {
            override fun parseRow(columns: Map<String, Any?>): RawSchedule {
                return RawSchedule(columns[Columns.TIMESTAMP] as Long, columns[Columns.DATA] as String)
            }
        }
    }

    private val preferences = context.schedulePreferences

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_RAW_SCHEDULE, true,
                Columns.TIMESTAMP to INTEGER + PRIMARY_KEY,
                Columns.GROUP to TEXT,
                Columns.DATA to TEXT + UNIQUE,
                Columns.HASH to INTEGER)
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) {
            db.dropTable(TABLE_RAW_SCHEDULE)
            createTables(db)
            return
        }
    }

    fun request(group: String, hash: Int? = null): List<RawSchedule> {
        return execute {
            val query = select(TABLE_RAW_SCHEDULE).orderBy(Columns.TIMESTAMP)

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
            val contentValues = ContentValues().apply {
                put(Columns.GROUP, group)
                put(Columns.TIMESTAMP, raw.timestamp)
                put(Columns.DATA, raw.data)
                put(Columns.HASH, hash)
            }
            insertWithOnConflict(TABLE_RAW_SCHEDULE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)

            execSQL("DELETE FROM $TABLE_RAW_SCHEDULE " +
                    "WHERE ${Columns.GROUP} = ? AND ${Columns.TIMESTAMP} IN (" +
                    "SELECT ${Columns.TIMESTAMP} FROM $TABLE_RAW_SCHEDULE " +
                    "ORDER BY ${Columns.TIMESTAMP} DESC " +
                    "LIMIT -1 OFFSET ?" +
                    ")", arrayOf(group, preferences.historySize))
        }
    }

    fun remove(group: String, raw: RawSchedule) {
        execute {
            delete(TABLE_RAW_SCHEDULE, "${Columns.GROUP} = ? AND ${Columns.TIMESTAMP} = ?",
                    arrayOf(group, raw.timestamp.toString()))
        }
    }

}
