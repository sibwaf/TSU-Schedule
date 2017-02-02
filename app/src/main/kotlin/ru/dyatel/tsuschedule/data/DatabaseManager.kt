package ru.dyatel.tsuschedule.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.dyatel.tsuschedule.events.EventBus

private const val DB_FILE = "data.db"
private const val DB_VERSION = 3

fun getDropTableQuery(name: String) = "DROP TABLE IF EXISTS " + name

class DatabaseManager(context: Context, eventBus: EventBus) : SQLiteOpenHelper(context, DB_FILE, null, DB_VERSION) {

    val lessonDAO = LessonDAO(this, eventBus)

    private val parts = mutableSetOf(lessonDAO)

    override fun onCreate(db: SQLiteDatabase) {
        parts.forEach { it.createTables(db) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        parts.forEach { it.upgradeTables(db, oldVersion, newVersion) }
    }

}
