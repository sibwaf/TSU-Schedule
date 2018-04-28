package ru.dyatel.tsuschedule.database

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.dropTable
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.utilities.schedulePreferences

private const val DB_FILE = "data.db"
private const val DB_VERSION = 7

class DatabaseManager(private val context: Context) : ManagedSQLiteOpenHelper(context, DB_FILE, version = DB_VERSION) {

    val changelogs = ChangelogDao(this)

    val snapshots = ScheduleSnapshotDao(context, this)

    val filters = FilterDao(context, this)
    val rawGroupSchedule = RawGroupScheduleDao(context, this)
    val filteredGroupSchedule = FilteredGroupScheduleDao(context, this)

    val teachers = TeacherDao(this)
    val teacherSchedule = TeacherScheduleDao(this)

    val exams = ExamScheduleDao(this)

    private val parts = setOf(
            changelogs,
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

        if (oldVersion < 7) {
            val oldSchedule = UnfilteredGroupScheduleDao(context, this)
            oldSchedule.upgradeTables(db, oldVersion, newVersion)

            val preferences = context.schedulePreferences
            preferences.groups.forEach {
                val lessons = oldSchedule.request(it)
                if (lessons.isNotEmpty()) {
                    snapshots.save(it, lessons)
                }
            }

            db.dropTable(oldSchedule.table)
        }
    }

}

val Activity.database
    get() = (application as ScheduleApplication).database
