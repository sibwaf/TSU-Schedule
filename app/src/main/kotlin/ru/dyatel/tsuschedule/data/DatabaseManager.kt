package ru.dyatel.tsuschedule.data

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_FILE = "data.db"
private const val DB_VERSION = 3

class DatabaseManager(activity: Activity) : SQLiteOpenHelper(activity, DB_FILE, null, DB_VERSION) {

    val lessonDAO = LessonDAO(activity, this)

    override fun onCreate(db: SQLiteDatabase?) {
        lessonDAO.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        lessonDAO.onUpgrade(db, oldVersion, newVersion)
    }

}
