package ru.dyatel.tsuschedule.layout

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import ru.dyatel.tsuschedule.data.Lesson
import java.util.ArrayList

class LessonListAdapter : RecyclerView.Adapter<LessonListAdapter.Holder>() {

    class Holder(val view: GroupLessonView) : RecyclerView.ViewHolder(view)

    private val lessons = ArrayList<Lesson>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(GroupLessonView(parent.context))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.view.bind(lessons[position])
    }

    override fun getItemCount() = lessons.size

    fun updateData(lessons: List<Lesson>) {
        with(this.lessons) {
            clear()
            addAll(lessons)
        }
        notifyDataSetChanged()
    }

}