package ru.dyatel.tsuschedule.layout

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.parsing.Lesson
import java.util.ArrayList

class LessonListAdapter(private val activity: Activity) : RecyclerView.Adapter<LessonListAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {

        val color: View = v.findViewById(R.id.color)

        val time = v.findViewById(R.id.time) as TextView
        val auditory = v.findViewById(R.id.auditory) as TextView
        val discipline = v.findViewById(R.id.discipline) as TextView
        val teacher = v.findViewById(R.id.teacher) as TextView

    }

    val lessons = ArrayList<Lesson>()

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
        holder.color.setBackgroundResource(markerColor)
        holder.time.text = lesson.time
        holder.auditory.text = lesson.auditory
        holder.discipline.text = lesson.discipline
        holder.teacher.text = lesson.teacher

        holder.auditory.visibility = if (lesson.auditory.isNullOrEmpty()) View.GONE else View.VISIBLE
        holder.teacher.visibility = if (lesson.teacher.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = lessons.size

    fun updateData(lessons: List<Lesson>) {
        this.lessons.clear()
        this.lessons.addAll(lessons)

        activity.runOnUiThread { notifyDataSetChanged() }
    }

}