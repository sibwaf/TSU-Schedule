package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import ru.dyatel.tsuschedule.EmptyResultException
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.DatabaseManager
import ru.dyatel.tsuschedule.data.GroupLesson
import ru.dyatel.tsuschedule.data.currentWeekParity
import ru.dyatel.tsuschedule.data.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.GroupLessonView
import ru.dyatel.tsuschedule.layout.WeekDataContainer
import ru.dyatel.tsuschedule.layout.WeekPagerAdapter
import ru.dyatel.tsuschedule.parsing.DataRequester
import ru.dyatel.tsuschedule.parsing.GroupScheduleParser
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
        inflate(context, R.layout.schedule, this)

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

    fun bindData(weeks: WeekDataContainer<GroupLesson>) {
        val adapter = WeekPagerAdapter(context, weeks)
        pager.adapter = adapter
        pager.currentItem = adapter.getPosition(currentWeekParity)
    }

}

class ScheduleScreen(private val group: String) : Screen<ScheduleView>(), EventListener {

    private val weeks = WeekDataContainer { GroupLessonView(it) }

    private lateinit var database: DatabaseManager

    override fun createView(context: Context) = ScheduleView(context).apply { bindData(weeks) }

    override fun onShow(context: Context) {
        super.onShow(context)

        database = activity.database
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

    override fun getTitle(context: Context) = group

    fun fetchLessons() {
        launch(UI) {
            try {
                val preferences = ctx?.schedulePreferences ?: return@launch

                AsyncFetchStateKeeper.setState(group, true)

                val group = group
                val database = database

                async {
                    val requester = DataRequester().apply { timeout = preferences.connectionTimeout }
                    val schedule = requester.groupSchedule(group)

                    val data = GroupScheduleParser.parse(schedule)
                            .takeIf { it.isNotEmpty() } ?: throw EmptyResultException()
                    val hash = data.hashCode()

                    database.rawLessons.save(group, schedule, hash)
                    if (database.rawLessons.request(group, hash).count() > 1) {
                        TODO()
                    }

                    if (group in preferences.groups) {
                        database.lessons.update(group, data)
                    }
                }.await()
            } catch (e: Exception) {
                val view = AsyncFetchStateKeeper.getView(group)
                e.handle { message -> view?.let { longSnackbar(it, message) } }
            } finally {
                AsyncFetchStateKeeper.setState(group, false)
            }
        }
    }

    private fun loadLessons() {
        weeks.updateData(database.lessons.request(group))
    }

    override fun handleEvent(type: Event, payload: Any?) {
        if (payload as String != group) {
            return
        }

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

    fun getState(group: String) = states.getOrPut(group, { false })

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
