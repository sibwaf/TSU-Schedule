package ru.dyatel.tsuschedule.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.withArguments
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ScheduleApplication
import ru.dyatel.tsuschedule.data.LessonDao
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.WeekdayListAdapter
import ru.dyatel.tsuschedule.data.Parity
import ru.dyatel.tsuschedule.schedulePreferences

private const val PARITY_ARGUMENT = "parity"

class WeekFragment : Fragment(), EventListener {

    companion object {
        fun getInstance(parity: Parity) = WeekFragment().withArguments(PARITY_ARGUMENT to parity.toString())
    }

    private lateinit var parity: Parity

    private lateinit var weekdays: WeekdayListAdapter
    private lateinit var lessonDao: LessonDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parity = Parity.valueOf(arguments.getString(PARITY_ARGUMENT))

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

        with(view.find<RecyclerView>(R.id.weekday_list)) {
            layoutManager = LinearLayoutManager(ctx)
            adapter = weekdays
        }

        return view
    }

    override fun handleEvent(type: Event, payload: Any?) = refresh()

    private fun refresh() {
        doAsync {
            val lessons = lessonDao.request(ctx.schedulePreferences.subgroup)
                    .filter { it.parity == parity || it.parity == Parity.BOTH }
                    .toCollection(ArrayList())
            weekdays.updateData(lessons)
        }
    }

}
