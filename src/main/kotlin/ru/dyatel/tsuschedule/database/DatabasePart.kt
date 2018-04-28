package ru.dyatel.tsuschedule.database

import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.transaction

abstract class DatabasePart(protected val databaseManager: DatabaseManager) {

    protected companion object {
        fun Boolean.toInt() = if (this) 1 else 0
        fun Long.asFlag() = this != 0L
    }

    protected fun <T> execute(block: SQLiteDatabase.() -> T): T = databaseManager.use(block)

    protected fun <T> executeTransaction(block: SQLiteDatabase.() -> T): T {
        return execute {
            var result: T? = null
            transaction {
                result = block()
            }
            result!!
        }
    }

    abstract fun createTables(db: SQLiteDatabase)

    abstract fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)

}
