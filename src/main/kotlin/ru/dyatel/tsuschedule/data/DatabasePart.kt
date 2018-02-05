package ru.dyatel.tsuschedule.data

import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.transaction

abstract class DatabasePart(protected val databaseManager: DatabaseManager) {

    protected fun <T> execute(block: SQLiteDatabase.() -> T): T = databaseManager.use(block)

    protected fun executeTransaction(block: SQLiteDatabase.() -> Unit) = execute { transaction(block) }

    abstract fun createTables(db: SQLiteDatabase)

    abstract fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)

}
