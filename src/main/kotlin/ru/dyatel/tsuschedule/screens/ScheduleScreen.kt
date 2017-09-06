package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.longToast
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.Parser
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.Lesson
import ru.dyatel.tsuschedule.data.LessonDao
import ru.dyatel.tsuschedule.data.Parity
import ru.dyatel.tsuschedule.data.currentWeekParity
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.WeekDataContainer
import ru.dyatel.tsuschedule.layout.WeekPagerAdapter
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class ScheduleView(context: Context) : BaseScreenView<ScheduleScreen>(context) {

    private val pager: ViewPager

    private val swipeRefresh: SwipeRefreshLayout
    private var blockSwipeRefresh = false
    var isRefreshing: Boolean
        get() = swipeRefresh.isRefreshing
        set(value) {
            swipeRefresh.isRefreshing = value
        }

    private val pagerScrollListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {
            blockSwipeRefresh = state != ViewPager.SCROLL_STATE_IDLE
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) = Unit

    }

    init {
        inflate(context, R.layout.main_screen, this)

        pager = find<ViewPager>(R.id.pager).apply {
            addOnPageChangeListener(pagerScrollListener)
        }

        find<TabLayout>(R.id.tab_layout).apply {
            ViewCompat.setElevation(this, resources.getDimension(R.dimen.elevation))
            setupWithViewPager(pager)
        }

        swipeRefresh = find<SwipeRefreshLayout>(R.id.swipe_refresh).apply {
            setOnRefreshListener { screen.updateData() }
            setOnChildScrollUpCallback { _, _ -> blockSwipeRefresh }
        }
    }

    fun bindData(weeks: WeekDataContainer) {
        val adapter = WeekPagerAdapter(context, weeks)
        pager.adapter = adapter
        pager.currentItem = adapter.getPosition(currentWeekParity)
    }

}

class ScheduleScreen : Screen<ScheduleView>(), EventListener {

    private val context: Context
        get() = activity

    private val weeks = WeekDataContainer()

    private lateinit var lessons: LessonDao

    override fun createView(context: Context) = ScheduleView(context).apply { bindData(weeks) }

    override fun onShow(context: Context?) {
        super.onShow(context)

        lessons = (activity.application as ScheduleApplication).databaseManager.lessonDao
        EventBus.subscribe(this, Event.DATA_UPDATE_FAILED, Event.DATA_UPDATED)
        doAsync { handleEvent(Event.DATA_UPDATED, null) }
    }

    override fun onHide(context: Context?) {
        EventBus.unsubscribe(this)
        super.onHide(context)
    }

    fun updateData() = doAsync {
        val preferences = context.schedulePreferences

        val parser = Parser()
        parser.setTimeout(preferences.connectionTimeout * 1000)

        try {
            val data = parser.getLessons(preferences.group)
            lessons.update(data)
        } catch (e: Exception) {
            EventBus.broadcast(Event.DATA_UPDATE_FAILED)
            uiThread {
                e.handle { context.longToast(it) }
            }
        }
    }

    override fun handleEvent(type: Event, payload: Any?) {
        if (type == Event.DATA_UPDATED) {
            val odd = mutableListOf<Lesson>()
            val even = mutableListOf<Lesson>()
            lessons.request(context.schedulePreferences.subgroup).forEach {
                when (it.parity) {
                    Parity.BOTH -> {
                        odd += it
                        even += it
                    }
                    Parity.ODD -> odd += it
                    Parity.EVEN -> even += it
                }
            }
            context.runOnUiThread { weeks.updateData(odd, even) } // TODO: do not update data on UI thread
        }

        context.runOnUiThread { view.isRefreshing = false }
    }
}
