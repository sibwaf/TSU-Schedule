package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.EmptyResultException
import ru.dyatel.tsuschedule.Parser
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.LessonDao
import ru.dyatel.tsuschedule.data.Parity
import ru.dyatel.tsuschedule.data.currentWeekParity
import ru.dyatel.tsuschedule.data.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.WeekDataContainer
import ru.dyatel.tsuschedule.layout.WeekPagerAdapter
import ru.dyatel.tsuschedule.utilities.ctx
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
            setOnRefreshListener { screen.fetchLessons() }
            setOnChildScrollUpCallback { _, _ -> blockSwipeRefresh }
        }
    }

    fun bindData(weeks: WeekDataContainer) {
        val adapter = WeekPagerAdapter(context, weeks)
        pager.adapter = adapter
        pager.currentItem = adapter.getPosition(currentWeekParity)
    }

}

class ScheduleScreen(private val group: String) : Screen<ScheduleView>(), EventListener {

    private val weeks = WeekDataContainer()

    private lateinit var lessons: LessonDao

    override fun createView(context: Context) = ScheduleView(context).apply { bindData(weeks) }

    override fun onShow(context: Context) {
        super.onShow(context)

        lessons = activity.database.lessons
        context.schedulePreferences.group = group

        loadLessons()
        AsyncFetchStateKeeper.attachView(group, view)

        EventBus.subscribe(this, Event.INITIAL_DATA_FETCH, Event.DATA_UPDATED)
    }

    override fun onHide(context: Context?) {
        AsyncFetchStateKeeper.detachView(group)
        EventBus.unsubscribe(this)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_schedule, group)!!

    fun fetchLessons() {
        val preferences = ctx!!.schedulePreferences

        AsyncFetchStateKeeper.setState(group, true)

        doAsync {
            try {
                val parser = Parser().apply { setTimeout(preferences.connectionTimeout) }
                val data = parser.getLessons(group).takeIf { it.isNotEmpty() }
                        ?: throw EmptyResultException()

                if (group in preferences.groups)
                    lessons.update(group, data)
            } catch (e: Exception) {
                uiThread {
                    val view = AsyncFetchStateKeeper.getView(group)
                    if (view != null) {
                        e.handle { longSnackbar(view, it) }
                    } else {
                        e.handle()
                    }
                }
            } finally {
                uiThread { AsyncFetchStateKeeper.setState(group, false) }
            }
        }
    }

    private fun loadLessons() {
        val (odd, even) = lessons.request(group).partition { it.parity == Parity.ODD }
        weeks.updateData(odd, even)
    }

    override fun handleEvent(type: Event, payload: Any?) {
        if (payload as String != group)
            return

        ctx!!.runOnUiThread {
            when (type) {
                Event.INITIAL_DATA_FETCH -> fetchLessons()
                Event.DATA_UPDATED -> loadLessons()
            }
        }
    }

    override fun onUpdateMenu(menu: Menu) {
        menu.findItem(R.id.filters).isVisible = true
        menu.findItem(R.id.delete_group).isVisible = true
    }

}

private object AsyncFetchStateKeeper {

    private val states = mutableMapOf<String, Boolean>()
    private val views = mutableMapOf<String, ScheduleView>()

    fun getState(group: String) = states.getOrDefault(group, false)

    fun setState(group: String, state: Boolean) {
        states[group] = state
        views[group]?.isRefreshing = state
    }

    fun getView(group: String) = views[group]

    fun attachView(group: String, view: ScheduleView) {
        views[group] = view
        view.isRefreshing = getState(group)
    }

    fun detachView(group: String) {
        views.remove(group)
    }

}
