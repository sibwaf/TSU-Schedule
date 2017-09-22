package ru.dyatel.tsuschedule.data

import android.database.sqlite.SQLiteDatabase

abstract class DatabasePart(protected val databaseManager: DatabaseManager) {

    protected val readableDatabase
        get() = databaseManager.readableDatabase!!

    protected val writableDatabase
        get() = databaseManager.writableDatabase!!

    abstract fun createTables(db: SQLiteDatabase)

    abstract fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)

}
