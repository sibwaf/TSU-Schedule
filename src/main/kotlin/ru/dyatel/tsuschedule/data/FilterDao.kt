package ru.dyatel.tsuschedule.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.AUTOINCREMENT
import org.jetbrains.anko.db.FOREIGN_KEY
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.LongParser
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.PRIMARY_KEY
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.createTable
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import kotlin.reflect.KClass

class FilterDao(private val context: Context, databaseManager: DatabaseManager) : DatabasePart(databaseManager) {

    private object FilterColumns {
        const val ID = "filter_id"
        const val GROUP = "`group`"
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

    override fun createTables(db: SQLiteDatabase) {
        db.createTable(TABLE_FILTERS, true,
                FilterColumns.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                FilterColumns.GROUP to TEXT,
                FilterColumns.TYPE to TEXT,
                FilterColumns.ENABLED to TEXT)
        db.createTable(TABLE_DATA, true,
                DataColumns.ID to INTEGER,
                DataColumns.KEY to TEXT,
                DataColumns.VALUE to TEXT,
                FOREIGN_KEY(DataColumns.ID, TABLE_FILTERS, FilterColumns.ID))
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            db.dropTable(TABLE_DATA, true)
            db.dropTable(TABLE_FILTERS, true)
            createTables(db)
            return
        }

        val preferences = context.schedulePreferences

        if (oldVersion < 6) {
            val group = preferences.group
            if (group != null && group in preferences.groups) {
                val type = TEXT.render()
                db.execSQL("ALTER TABLE $TABLE_FILTERS ADD COLUMN ${FilterColumns.GROUP} $type")
                db.update(TABLE_FILTERS, FilterColumns.GROUP to group).exec()
            } else {
                db.dropTable(TABLE_DATA, true)
                db.dropTable(TABLE_FILTERS, true)
                createTables(db)
                return
            }
        }
    }

    fun update(group: String, filters: List<Filter>) {
        // TODO: validate filter order

        executeTransaction {
            remove(group)

            filters.forEach {
                if (it !is PredefinedFilter) TODO()

                val type = PredefinedFilters.toType(it)

                val id = insert(TABLE_FILTERS,
                        FilterColumns.GROUP to group,
                        FilterColumns.TYPE to type,
                        FilterColumns.ENABLED to it.enabled.toString())

                it.save().forEach { (key, value) ->
                    insert(TABLE_DATA,
                            DataColumns.ID to id,
                            DataColumns.KEY to key,
                            DataColumns.VALUE to value)
                }
            }
        }

        EventBus.broadcast(Event.DATA_MODIFIER_SET_CHANGED)
    }

    fun remove(group: String) {
        executeTransaction {
            val ids = select(TABLE_FILTERS, FilterColumns.ID)
                    .whereSimple("${FilterColumns.GROUP} = ?", group)
                    .parseList(LongParser)
                    .joinToString(", ")

            delete(TABLE_DATA, "${DataColumns.ID} IN ($ids)")
            delete(TABLE_FILTERS, "${FilterColumns.GROUP} = ?", arrayOf(group))
        }
    }

    fun request(group: String): List<Filter> {
        return execute {
            val (normal, predefined) = select(TABLE_FILTERS)
                    .whereSimple("${FilterColumns.GROUP} = ?", group)
                    .orderBy(FilterColumns.ID)
                    .parseList(FILTER_PARSER)
                    .map { (id, filter) ->
                        if (filter !is PredefinedFilter) TODO()

                        val data = select(TABLE_DATA)
                                .whereSimple("${DataColumns.ID} = $id")
                                .parseList(DATA_PARSER)
                                .toMap()

                        filter.apply { load(data) }
                    }
                    .partition { it !is PredefinedFilter }

            normal + PredefinedFilters.ensurePresenceAndOrder(predefined)
        }
    }

}
