package ru.dyatel.tsuschedule.layout

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sp
import org.jetbrains.anko.textView
import ru.dyatel.tsuschedule.data.Teacher

class TeacherListAdapter : RecyclerView.Adapter<TeacherListAdapter.Holder>() {

    private companion object {
        val nameViewId = View.generateViewId()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView = view.find<TextView>(nameViewId)
    }

    private val teachers = mutableListOf<Teacher>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = parent.context.linearLayout {
            lparams(width = matchParent)

            textView {
                id = nameViewId
                textSize = sp(7).toFloat()
            }
        }
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.nameView.text = teachers[position].name
    }

    override fun getItemCount() = teachers.size

    fun updateData(teachers: List<Teacher>) {
        with(this.teachers) {
            clear()
            addAll(teachers)
        }
        notifyDataSetChanged()
    }

    fun getData(): List<Teacher> = teachers

}
