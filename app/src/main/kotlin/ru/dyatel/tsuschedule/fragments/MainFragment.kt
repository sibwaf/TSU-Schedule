package ru.dyatel.tsuschedule.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.Lesson
import ru.dyatel.tsuschedule.data.LessonDao
import ru.dyatel.tsuschedule.data.Parity
import ru.dyatel.tsuschedule.data.asyncLessonFetch
import ru.dyatel.tsuschedule.data.currentWeekParity
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.WeekDataContainer
import ru.dyatel.tsuschedule.layout.WeekPagerAdapter
import ru.dyatel.tsuschedule.schedulePreferences

class MainFragment : Fragment(), EventListener {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var blockSwipeRefresh = false

    private lateinit var lessons: LessonDao

    private val weeks = WeekDataContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lessons = (activity.application as ScheduleApplication).databaseManager.lessonDao

        EventBus.subscribe(this, Event.DATA_UPDATE_FAILED, Event.DATA_UPDATED)
        doAsync { handleEvent(Event.DATA_UPDATED, null) }
    }

    override fun onDestroy() {
        EventBus.unsubscribe(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.main_screen, container, false)

        val pager = root.find<ViewPager>(R.id.pager).apply {
            val _adapter = WeekPagerAdapter(ctx, weeks)
            adapter = _adapter
            currentItem = _adapter.getPosition(currentWeekParity)
            addOnPageChangeListener(pagerScrollListener)
        }

        val tabLayout = root.find<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(pager)

        val lessonDao = (activity.application as ScheduleApplication).databaseManager.lessonDao
        swipeRefresh = root.find(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener { asyncLessonFetch(ctx, lessonDao) }
        swipeRefresh.setOnChildScrollUpCallback { _, _ -> blockSwipeRefresh }

        return root
    }

    override fun handleEvent(type: Event, payload: Any?) {
        if (type == Event.DATA_UPDATED) {
            val odd = mutableListOf<Lesson>()
            val even = mutableListOf<Lesson>()
            lessons.request(ctx.schedulePreferences.subgroup).forEach {
                when (it.parity) {
                    Parity.BOTH -> {
                        odd += it
                        even += it
                    }
                    Parity.ODD -> odd += it
                    Parity.EVEN -> even += it
                }
            }
            runOnUiThread {
                weeks.updateData(odd, even)
                swipeRefresh.isRefreshing = false
            }
        } else runOnUiThread { swipeRefresh.isRefreshing = false }
    }

    private val pagerScrollListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {
            blockSwipeRefresh = state != ViewPager.SCROLL_STATE_IDLE
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) = Unit

    }

}
