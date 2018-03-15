package ru.dyatel.tsuschedule.data

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import ru.dyatel.tsuschedule.ScheduleApplication

private const val DB_FILE = "data.db"
private const val DB_VERSION = 7

class DatabaseManager(context: Context) :
        ManagedSQLiteOpenHelper(context, DB_FILE, version = DB_VERSION) {

    val lessons = LessonDao(context, this)
    val filters = FilterDao(context, this)
    val teachers = TeacherDao(this)

    private val parts = setOf(lessons, filters, teachers)

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        parts.forEach { it.createTables(db) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        parts.forEach { it.upgradeTables(db, oldVersion, newVersion) }
    }

}

val Activity.database
    get() = (application as ScheduleApplication).database
