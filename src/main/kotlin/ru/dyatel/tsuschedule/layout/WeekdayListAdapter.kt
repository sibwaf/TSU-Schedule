package ru.dyatel.tsuschedule.layout

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

private typealias Weekday = Pair<String, List<Lesson>>

class WeekdayListAdapter : RecyclerView.Adapter<WeekdayListAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {

        val weekdayName = v.find<TextView>(R.id.weekday)
        private val lessonListAdapter = LessonListAdapter()

        init {
            v.find<RecyclerView>(R.id.lesson_list).apply {
                ViewCompat.setNestedScrollingEnabled(this, false)
                layoutManager = LinearLayoutManager(v.context)
                adapter = lessonListAdapter
            }
        }

        fun updateData(lessons: List<Lesson>) = lessonListAdapter.updateData(lessons)

    }

    private val weekdays = ArrayList<Weekday>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weekday, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (weekday, lessons) = weekdays[position]
        holder.weekdayName.text = weekday
        holder.updateData(lessons)
    }

    override fun getItemCount() = weekdays.size

    // TODO: should be callable from any thread
    fun updateData(lessons: List<Lesson>) {
        weekdays.clear()

        NORMAL_WEEKDAY_ORDER.forEach { weekday ->
            lessons.filter { it.weekday.toLowerCase() == weekday }
                    .takeIf { it.isNotEmpty() }
                    ?.let { weekdays += weekday to it.toList() }
        }

        notifyDataSetChanged()
    }

}
