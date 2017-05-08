package ru.dyatel.tsuschedule.layout

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.parsing.Lesson
import java.util.ArrayList

class LessonListAdapter(private val activity: Activity) : RecyclerView.Adapter<LessonListAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {

        val color: View = v.find<View>(R.id.color)

        val time = v.find<TextView>(R.id.time)
        val auditory = v.find<TextView>(R.id.auditory)
        val discipline = v.find<TextView>(R.id.discipline)
        val teacher = v.find<TextView>(R.id.teacher)

    }

    private val lessons = ArrayList<Lesson>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val lesson = lessons[position]

        val markerColor = when (lesson.type) {
            Lesson.Type.PRACTICE -> R.color.practice_color
            Lesson.Type.LECTURE -> R.color.lecture_color
            Lesson.Type.LABORATORY -> R.color.laboratory_color
            Lesson.Type.UNKNOWN -> R.color.unknown_color
        }

        with(holder) {
            color.setBackgroundResource(markerColor)
            time.text = lesson.time
            auditory.text = lesson.auditory
            discipline.text = lesson.discipline
            teacher.text = lesson.teacher

            auditory.visibility = if (lesson.auditory.isNullOrEmpty()) View.GONE else View.VISIBLE
            teacher.visibility = if (lesson.teacher.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = lessons.size

    fun updateData(lessons: List<Lesson>) {
        this.lessons.clear()
        this.lessons.addAll(lessons)

        activity.runOnUiThread { notifyDataSetChanged() }
    }

}