package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.select
import ru.dyatel.tsuschedule.model.VersionChangelog
import ru.dyatel.tsuschedule.updater.ReleaseToken

class ChangelogDao(databaseManager: DatabaseManager) : DatabasePart(databaseManager) {

    private object ChangelogColumns {
        const val VERSION = "version"
        const val PRERELEASE = "prerelease"
        const val CHANGES = "changes"
    }

    private companion object {
        const val TABLE = "changelog"

        val CHANGELOG_PARSER = object : MapRowParser<VersionChangelog> {
            override fun parseRow(columns: Map<String, Any?>): VersionChangelog {
                return VersionChangelog(
                        columns[ChangelogColumns.VERSION] as String,
                        (columns[ChangelogColumns.PRERELEASE] as Long).asFlag(),
                        columns[ChangelogColumns.CHANGES] as String)
            }
        }
    }

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE, true,
                ChangelogColumns.VERSION to TEXT,
                ChangelogColumns.CHANGES to TEXT)
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.dropTable(TABLE, true)
            createTables(db)
        }
    }

    fun request(): List<VersionChangelog> {
        return execute {
            select(TABLE)
                    .orderBy(ChangelogColumns.VERSION, SqlOrderDirection.DESC)
                    .parseList(CHANGELOG_PARSER)
        }
    }

    fun save(releases: Collection<ReleaseToken>) {
        executeTransaction {
            delete(TABLE)

            releases.forEach {
                val contentValues = ContentValues().apply {
                    put(ChangelogColumns.VERSION, it.release.version)
                    put(ChangelogColumns.PRERELEASE, it.prerelease.toInt())
                    put(ChangelogColumns.CHANGES, it.changes)
                }

                insert(TABLE, null, contentValues)
            }
        }
    }

}