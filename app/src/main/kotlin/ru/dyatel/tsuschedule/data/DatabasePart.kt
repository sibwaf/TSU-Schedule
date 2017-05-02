package ru.dyatel.tsuschedule.data

import android.database.sqlite.SQLiteDatabase

interface DatabasePart {

    fun createTables(db: SQLiteDatabase)
    fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)

}