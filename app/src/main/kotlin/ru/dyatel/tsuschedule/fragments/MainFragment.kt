package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hirondelle.date4j.DateTime
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.ParityReference
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.LessonFetchTask
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.WeekFragmentPagerAdapter
import ru.dyatel.tsuschedule.parsing.DateUtil
import java.util.TimeZone

class MainFragment : Fragment(), EventListener {

    private lateinit var weekAdapter: WeekFragmentPagerAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var eventBus: EventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weekAdapter = WeekFragmentPagerAdapter(childFragmentManager)

        val application = activity.application as ScheduleApplication
        eventBus = application.eventBus

        eventBus.subscribe(this, Event.DATA_UPDATE_FAILED, Event.DATA_UPDATED)
    }

    override fun onDestroy() {
        eventBus.unsubscribe(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.main_screen, container, false)

        val pager = root.find<ViewPager>(R.id.pager)
        pager.adapter = weekAdapter
        pager.currentItem = ParityReference.getIndexFromParity(
                DateUtil.getWeekParity(DateTime.now(TimeZone.getDefault()))
        )

        val tabLayout = root.find<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(pager)

        val lessonDao = (activity.application as ScheduleApplication).databaseManager.lessonDao
        swipeRefresh = root.find<SwipeRefreshLayout>(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener { LessonFetchTask(context, eventBus, lessonDao).execute() }
        swipeRefresh.setOnChildScrollUpCallback { _, _ ->
            val week = weekAdapter.getFragment(pager.currentItem)
            week.view?.canScrollVertically(-1) ?: false
        }

        return root
    }

    override fun handleEvent(type: Event) = activity.runOnUiThread { swipeRefresh.isRefreshing = false }

}
