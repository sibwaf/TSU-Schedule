package ru.dyatel.tsuschedule.data

import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.FOREIGN_KEY
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.PRIMARY_KEY
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus

private const val TABLE_FILTERS = "filters"
private const val TABLE_DATA = "filter_data"

private object FilterColumns {

    const val ID = "filter_id"
    const val TYPE = "type"
    const val ENABLED = "enabled"

}

private object FilterDataColumns {

    const val ID = FilterColumns.ID
    const val KEY = "key"
    const val VALUE = "value"

}

class FilterDao(private val databaseManager: DatabaseManager) : DatabasePart {

    private val readableDatabase
        get() = databaseManager.readableDatabase
    private val writableDatabase
        get() = databaseManager.writableDatabase

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_FILTERS, true,
                FilterColumns.ID to INTEGER + PRIMARY_KEY,
                FilterColumns.TYPE to TEXT,
                FilterColumns.ENABLED to TEXT
        )
        db.createTable(TABLE_DATA, true,
                FilterDataColumns.ID to INTEGER,
                FilterDataColumns.KEY to TEXT,
                FilterDataColumns.VALUE to TEXT,
                FOREIGN_KEY(FilterDataColumns.ID, TABLE_FILTERS, FilterColumns.ID)
        )
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TABLE_DATA, true)
        db.dropTable(TABLE_FILTERS, true)
        createTables(db)
    }

    fun updateFilters(filters: List<Filter>, predefinedFilters: List<PredefinedFilter>) {
        writableDatabase.transaction {
            delete(TABLE_DATA)
            delete(TABLE_FILTERS)

            var id = 1
            if (filters.any()) TODO("Not implemented")

            predefinedFilters.forEach {
                insert(TABLE_FILTERS,
                        FilterColumns.ID to id,
                        FilterColumns.TYPE to it.getType().name,
                        FilterColumns.ENABLED to it.enabled.toString()
                )
                it.save().entries.forEach { (key, value) ->
                    insert(TABLE_DATA,
                            FilterDataColumns.ID to id,
                            FilterDataColumns.KEY to key,
                            FilterDataColumns.VALUE to value
                    )
                }

                id++
            }
        }
        EventBus.broadcast(Event.DATA_MODIFIER_SET_CHANGED)
    }

    fun getFilters() = emptyList<Filter>()

    fun getPredefinedFilters(): List<PredefinedFilter> = readableDatabase.use { database ->
        val list = database.select(TABLE_FILTERS)
                .orderBy(FilterColumns.ID)
                .parseList(FILTER_PARSER)
                .map { (id, filter) ->
                    filter as PredefinedFilter

                    val data = database.select(TABLE_DATA)
                            .whereSimple("${FilterDataColumns.ID} = ?", id.toString())
                            .parseList(FILTER_DATA_PARSER)
                            .toMap()

                    filter.apply { load(data) }
                }
                .toCollection(ArrayList())

        val types = list.map { it::class }.distinct()
        if (CommonPracticeFilter::class !in types) list += CommonPracticeFilter()
        if (SubgroupFilter::class !in types) list += SubgroupFilter()

        list
    }

}

private val FILTER_PARSER = object : MapRowParser<Pair<Int, Filter>> {

    override fun parseRow(columns: Map<String, Any?>): Pair<Int, Filter> {
        val id = (columns[FilterColumns.ID] as Long).toInt()
        val type = FilterType.valueOf(columns[FilterColumns.TYPE] as String)
        val enabled = (columns[FilterColumns.ENABLED] as String).toBoolean()

        val filter = when (type) {
            FilterType.COMMON_PRACTICE -> CommonPracticeFilter()
            FilterType.SUBGROUP -> SubgroupFilter()
        }.also { it.enabled = enabled }

        return id to filter
    }

}

private val FILTER_DATA_PARSER = object : MapRowParser<Pair<String, String>> {

    override fun parseRow(columns: Map<String, Any?>): Pair<String, String> {
        val key = columns[FilterDataColumns.KEY] as String
        val value = columns[FilterDataColumns.VALUE] as String
        return key to value
    }

}
