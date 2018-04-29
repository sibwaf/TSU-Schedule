package ru.dyatel.tsuschedule.database

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import ru.dyatel.tsuschedule.ScheduleApplication

private const val DB_FILE = "data.db"
private const val DB_VERSION = 7

class DatabaseManager(context: Context) : ManagedSQLiteOpenHelper(context, DB_FILE, version = DB_VERSION) {

    val changelogs = ChangelogDao(this)

    @Deprecated("Use snapshots")
    @Suppress("deprecation")
    val oldSchedule = UnfilteredGroupScheduleDao(context, this)

    val snapshots = ScheduleSnapshotDao(context, this)

    val filters = FilterDao(context, this)
    val rawGroupSchedule = RawGroupScheduleDao(context, this)
    val filteredGroupSchedule = FilteredGroupScheduleDao(context, this)

    val teachers = TeacherDao(this)
    val teacherSchedule = TeacherScheduleDao(this)

    val exams = ExamScheduleDao(this)

    @Suppress("deprecation")
    private val parts = setOf(
            changelogs,
            oldSchedule,
            filters,
            snapshots, rawGroupSchedule, filteredGroupSchedule,
            teachers, teacherSchedule,
            exams)

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
