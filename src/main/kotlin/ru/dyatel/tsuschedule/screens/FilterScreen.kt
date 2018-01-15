package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.view.ViewGroup
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.Filter
import ru.dyatel.tsuschedule.data.PredefinedFilter
import ru.dyatel.tsuschedule.data.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus

class FilterScreenView(context: Context) : BaseScreenView<FilterScreen>(context) {

    private val container: ViewGroup = verticalLayout()

    fun attachFilters(filters: List<Filter>, predefinedFilters: List<PredefinedFilter>) {
        if (filters.any()) TODO("Not implemented")

        container.removeAllViews()
        predefinedFilters
                .map { it.createView(context) }
                .forEach { container.addView(it) }
    }

}

class FilterScreen(private val group: String) : Screen<FilterScreenView>() {

    private lateinit var filters: List<Filter>
    private lateinit var predefinedFilters: List<PredefinedFilter>

    override fun createView(context: Context) = FilterScreenView(context)

    override fun onShow(context: Context) {
        super.onShow(context)
        EventBus.broadcast(Event.DISABLE_NAVIGATION_DRAWER)

        val separatedFilters = activity.database.filters.request(group)
                .partition { it !is PredefinedFilter }
        filters = separatedFilters.first
        predefinedFilters = separatedFilters.second.map { it as PredefinedFilter }

        view.attachFilters(filters, predefinedFilters)
    }

    override fun onHide(context: Context) {
        activity.database.filters.update(group, filters + predefinedFilters)

        EventBus.broadcast(Event.ENABLE_NAVIGATION_DRAWER)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_filters, group)!!
}
