package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.view.ViewGroup
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.database.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.layout.DIM_CARD_VERTICAL_MARGIN
import ru.dyatel.tsuschedule.model.Filter
import ru.dyatel.tsuschedule.model.PredefinedFilter

class FilterScreenView(context: Context) : BaseScreenView<FilterScreen>(context) {

    private val container: ViewGroup = verticalLayout {
        // TODO: scrolling?
        lparams(width = matchParent, height = matchParent) {
            clipToPadding = false
            topPadding = DIM_CARD_VERTICAL_MARGIN
            bottomPadding = DIM_CARD_VERTICAL_MARGIN
        }
    }

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
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, false)

        val separatedFilters = activity.database.filters.request(group)
                .partition { it !is PredefinedFilter }
        filters = separatedFilters.first
        predefinedFilters = separatedFilters.second.map { it as PredefinedFilter }

        view.attachFilters(filters, predefinedFilters)
    }

    override fun onHide(context: Context) {
        activity.database.filters.update(group, filters + predefinedFilters)

        EventBus.broadcast(Event.SET_DRAWER_ENABLED, true)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_filters, group)!!
}
