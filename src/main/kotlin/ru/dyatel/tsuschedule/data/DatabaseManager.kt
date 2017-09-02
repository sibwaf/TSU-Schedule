package ru.dyatel.tsuschedule.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper

private const val DB_FILE = "data.db"
private const val DB_VERSION = 4

class DatabaseManager(context: Context) :
        ManagedSQLiteOpenHelper(context, DB_FILE, version = DB_VERSION) {

    val lessonDao = LessonDao(this)

    private val parts = setOf(lessonDao)

    override fun onCreate(db: SQLiteDatabase) {
        parts.forEach { it.createTables(db) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        parts.forEach { it.upgradeTables(db, oldVersion, newVersion) }
    }

}
