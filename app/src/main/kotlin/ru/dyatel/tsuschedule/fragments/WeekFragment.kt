package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.LessonDao
import ru.dyatel.tsuschedule.data.getSubgroup
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.WeekdayListAdapter
import ru.dyatel.tsuschedule.parsing.Parity

class WeekFragment(private val parity: Parity) : Fragment(), EventListener {

    private lateinit var weekdays: WeekdayListAdapter

    private lateinit var lessonDao: LessonDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weekdays = WeekdayListAdapter(activity)

        lessonDao = (activity.application as ScheduleApplication).databaseManager.lessonDao

        EventBus.subscribe(this, Event.DATA_UPDATED)

        refresh()
    }

    override fun onDestroy() {
        EventBus.unsubscribe(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.week_fragment, container, false)

        val weekdayList = view.find<RecyclerView>(R.id.weekday_list)
        weekdayList.layoutManager = LinearLayoutManager(context)
        weekdayList.adapter = weekdays

        return view
    }

    override fun handleEvent(type: Event) = refresh()

    private fun refresh() {
        doAsync {
            val lessons = lessonDao.request(getSubgroup(context))
                    .filter { it.parity == parity || it.parity == Parity.BOTH }
                    .toCollection(ArrayList())
            weekdays.updateData(lessons)
        }
    }

}
