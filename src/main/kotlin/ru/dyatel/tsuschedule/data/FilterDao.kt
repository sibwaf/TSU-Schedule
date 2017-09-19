package ru.dyatel.tsuschedule.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class FilterDao(private val databaseManager: DatabaseManager, private val context: Context) : DatabasePart {

    override fun createTables(db: SQLiteDatabase) {
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun updateFilters(filters: List<Filter>, predefinedFilters: List<PredefinedFilter>) {
        // TODO: remove old filters

        filters.forEach { persist(it) }

        val preferences = context.schedulePreferences

        for (filter in predefinedFilters) {
            if (filter !is SubgroupFilter) throw TODO("Not implemented")

            preferences.subgroupFilterEnabled = filter.enabled
            preferences.subgroup = filter.subgroup
        }

        EventBus.broadcast(Event.DATA_MODIFIER_SET_CHANGED)
    }

    private fun persist(filter: Filter) {
        throw TODO("Not implemented")
    }

    fun getFilters() = emptyList<Filter>()

    fun getPredefinedFilters(): List<PredefinedFilter> {
        val preferences = context.schedulePreferences

        val subgroupFilter = SubgroupFilter(preferences.subgroupFilterEnabled, preferences.subgroup)
        return listOf(subgroupFilter)
    }

}
