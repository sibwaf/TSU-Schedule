package ru.dyatel.tsuschedule.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.LessonDAO
import ru.dyatel.tsuschedule.data.LessonFetchTask
import ru.dyatel.tsuschedule.data.getSubgroup
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.WeekdayListAdapter
import ru.dyatel.tsuschedule.parsing.Parity

class WeekFragment(private val parity: Parity) : Fragment(), EventListener {

    private inner class RefreshTask : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            swipeRefresh?.isRefreshing = true
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val lessons = lessonDao!!.request(getSubgroup(context))
            lessons.removeAll { it.parity != parity && it.parity != Parity.BOTH }
            weekdays!!.updateData(lessons)
            return null
        }

        override fun onPostExecute(result: Void?) {
            swipeRefresh?.isRefreshing = false
        }

    }

    private var swipeRefresh: SwipeRefreshLayout? = null
    private var weekdays: WeekdayListAdapter? = null

    private var eventBus: EventBus? = null
    private var lessonDao: LessonDAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weekdays = WeekdayListAdapter(activity)

        val application = activity.application as ScheduleApplication
        eventBus = application.eventBus
        lessonDao = application.databaseManager.lessonDAO

        eventBus!!.subscribe(this, Event.DATA_UPDATED, Event.DATA_UPDATE_FAILED)

        RefreshTask().execute()
    }

    override fun onDestroy() {
        eventBus!!.unsubscribe(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.week_fragment, container, false)

        val weekdayList = view.findViewById(R.id.weekday_list) as RecyclerView
        weekdayList.layoutManager = LinearLayoutManager(context)
        weekdayList.adapter = weekdays

        swipeRefresh = view.findViewById(R.id.swipe_refresh) as SwipeRefreshLayout
        swipeRefresh!!.setOnRefreshListener { LessonFetchTask(context, eventBus!!, lessonDao!!).execute() }

        return view
    }

    override fun handleEvent(type: Event) {
        when (type) {
            Event.DATA_UPDATED -> activity.runOnUiThread { RefreshTask().execute() }
            Event.DATA_UPDATE_FAILED -> activity.runOnUiThread { swipeRefresh?.isRefreshing = false }
        }
    }

}
