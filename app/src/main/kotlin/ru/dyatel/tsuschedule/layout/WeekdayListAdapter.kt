package ru.dyatel.tsuschedule.layout

import android.app.Activity
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.Lesson
import java.util.ArrayList

private val NORMAL_WEEKDAY_ORDER = arrayOf(
        "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье"
)

class WeekdayListAdapter(private val activity: Activity) : RecyclerView.Adapter<WeekdayListAdapter.Holder>() {

    class Holder(v: View, activity: Activity) : RecyclerView.ViewHolder(v) {

        val weekdayName = v.find<TextView>(R.id.weekday)
        val lessonList = v.find<RecyclerView>(R.id.lesson_list)

        val adapter = LessonListAdapter(activity)

        init {
            ViewCompat.setNestedScrollingEnabled(lessonList, false)
            lessonList.layoutManager = LinearLayoutManager(activity)
            lessonList.adapter = adapter
        }

    }

    private val weekdays = ArrayList<Pair<String, List<Lesson>>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weekday, parent, false)
        return Holder(view, activity)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (weekday, lessons) = weekdays[position]
        holder.weekdayName.text = weekday
        holder.adapter.updateData(lessons)
    }

    override fun getItemCount(): Int = weekdays.size

    fun updateData(lessons: List<Lesson>) {
        weekdays.clear()

        NORMAL_WEEKDAY_ORDER.forEach { weekday ->
            val weekdayLessons = lessons.filter { it.weekday.toLowerCase() == weekday }
                    .toCollection(ArrayList<Lesson>())
            if (weekdayLessons.size > 0) weekdays += weekday to weekdayLessons
        }

        activity.runOnUiThread { notifyDataSetChanged() }
    }

}
