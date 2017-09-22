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
import kotlin.reflect.KClass

class FilterDao(private val databaseManager: DatabaseManager) : DatabasePart {

    private object FilterColumns {
        const val ID = "filter_id"
        const val TYPE = "type"
        const val ENABLED = "enabled"
    }

    private object DataColumns {
        const val ID = FilterColumns.ID
        const val KEY = "key"
        const val VALUE = "value"
    }

    private companion object {

        const val TABLE_FILTERS = "filters"
        const val TABLE_DATA = "filter_data"

        val FILTER_PARSER = object : MapRowParser<Pair<Int, Filter>> {

            override fun parseRow(columns: Map<String, Any?>): Pair<Int, Filter> {
                val id = (columns[FilterColumns.ID] as Long).toInt()
                val type = columns[FilterColumns.TYPE] as String
                val enabled = (columns[FilterColumns.ENABLED] as String).toBoolean()

                return id to PredefinedFilters.fromType(type).also { it.enabled = enabled }
            }

        }

        val DATA_PARSER = object : MapRowParser<Pair<String, String>> {

            override fun parseRow(columns: Map<String, Any?>): Pair<String, String> {
                val key = columns[DataColumns.KEY] as String
                val value = columns[DataColumns.VALUE] as String
                return key to value
            }

        }

    }

    private object PredefinedFilters {

        private val classes = mutableListOf<KClass<out PredefinedFilter>>()
        private val types = mutableListOf<String>()

        init {
            add(CommonPracticeFilter::class, "common_practice")
            add(SubgroupFilter::class, "subgroup")
        }

        private fun add(c: KClass<out PredefinedFilter>, type: String) {
            classes += c
            types += type
        }

        fun toType(filter: PredefinedFilter): String {
            val index = classes.indexOf(filter::class).takeIf { it >= 0 }
                    ?: throw RuntimeException("Unknown filter type: $filter")
            return types[index]
        }

        fun fromType(type: String): PredefinedFilter {
            val index = types.indexOf(type).takeIf { it >= 0 }
                    ?: throw RuntimeException("Unknown filter type: $type")
            return classes[index].java.newInstance()
        }

        fun ensurePresenceAndOrder(filters: List<PredefinedFilter>): List<PredefinedFilter> {
            val presentClasses = filters.map { it::class }
            return classes.map {
                val index = presentClasses.indexOf(it)
                if (index == -1) it.java.newInstance() else filters[index]
            }
        }

    }

    private val readableDatabase
        get() = databaseManager.readableDatabase
    private val writableDatabase
        get() = databaseManager.writableDatabase

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_FILTERS, true,
                FilterColumns.ID to INTEGER + PRIMARY_KEY,
                FilterColumns.TYPE to TEXT,
                FilterColumns.ENABLED to TEXT)
        db.createTable(TABLE_DATA, true,
                DataColumns.ID to INTEGER,
                DataColumns.KEY to TEXT,
                DataColumns.VALUE to TEXT,
                FOREIGN_KEY(DataColumns.ID, TABLE_FILTERS, FilterColumns.ID))
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TABLE_DATA, true)
        db.dropTable(TABLE_FILTERS, true)
        createTables(db)
    }

    fun updateFilters(filters: List<Filter>) {
        // TODO: validate filter order

        writableDatabase.transaction {
            delete(TABLE_DATA)
            delete(TABLE_FILTERS)

            filters.forEachIndexed { index, filter ->
                if (filter !is PredefinedFilter) TODO()

                val type = PredefinedFilters.toType(filter)

                insert(TABLE_FILTERS,
                        FilterColumns.ID to index,
                        FilterColumns.TYPE to type,
                        FilterColumns.ENABLED to filter.enabled.toString())

                filter.save().entries.forEach { (key, value) ->
                    insert(TABLE_DATA,
                            DataColumns.ID to index,
                            DataColumns.KEY to key,
                            DataColumns.VALUE to value)
                }
            }
        }

        EventBus.broadcast(Event.DATA_MODIFIER_SET_CHANGED)
    }

    fun getFilters(): List<Filter> = readableDatabase.use { database ->
        val (normal, predefined) = database.select(TABLE_FILTERS)
                .orderBy(FilterColumns.ID)
                .parseList(FILTER_PARSER)
                .map { (id, filter) ->
                    if (filter !is PredefinedFilter) TODO()

                    val data = database.select(TABLE_DATA)
                            .whereSimple("${DataColumns.ID} = $id")
                            .parseList(DATA_PARSER)
                            .toMap()

                    filter.apply { load(data) }
                }
                .partition { it !is PredefinedFilter }

        normal + PredefinedFilters.ensurePresenceAndOrder(predefined)
    }

}
