package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.LessonDao
import ru.dyatel.tsuschedule.data.LessonFetchTask
import ru.dyatel.tsuschedule.data.getSubgroup
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.WeekdayListAdapter
import ru.dyatel.tsuschedule.parsing.Parity

class WeekFragment(private val parity: Parity) : Fragment(), EventListener {

    private var swipeRefresh: SwipeRefreshLayout? = null
    private lateinit var weekdays: WeekdayListAdapter

    private lateinit var eventBus: EventBus
    private lateinit var lessonDao: LessonDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weekdays = WeekdayListAdapter(activity)

        val application = activity.application as ScheduleApplication
        eventBus = application.eventBus
        lessonDao = application.databaseManager.lessonDao

        eventBus.subscribe(this, Event.DATA_UPDATED, Event.DATA_UPDATE_FAILED)

        refresh()
    }

    override fun onDestroy() {
        eventBus.unsubscribe(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.week_fragment, container, false)

        val weekdayList = view.find<RecyclerView>(R.id.weekday_list)
        weekdayList.layoutManager = LinearLayoutManager(context)
        weekdayList.adapter = weekdays

        swipeRefresh = view.find<SwipeRefreshLayout>(R.id.swipe_refresh)
        swipeRefresh!!.setOnRefreshListener { LessonFetchTask(context, eventBus, lessonDao).execute() }

        return view
    }

    override fun handleEvent(type: Event) {
        when (type) {
            Event.DATA_UPDATED -> activity.runOnUiThread { refresh() }
            Event.DATA_UPDATE_FAILED -> activity.runOnUiThread { swipeRefresh?.isRefreshing = false }
        }
    }

    private fun refresh() {
        swipeRefresh?.isRefreshing = true
        doAsync {
            val lessons = lessonDao.request(getSubgroup(context))
                    .filter { it.parity == parity || it.parity == Parity.BOTH }
                    .toCollection(ArrayList())
            weekdays.updateData(lessons)

            uiThread { swipeRefresh?.isRefreshing = false }
        }
    }

}
