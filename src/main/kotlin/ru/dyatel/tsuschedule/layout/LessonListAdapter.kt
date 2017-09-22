package ru.dyatel.tsuschedule.layout

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.Lesson
import ru.dyatel.tsuschedule.data.LessonType
import java.util.ArrayList

class LessonListAdapter : RecyclerView.Adapter<LessonListAdapter.Holder>() {

    private companion object {
        val typeViewId = View.generateViewId()
        val timeViewId = View.generateViewId()
        val auditoryViewId = View.generateViewId()
        val disciplineViewId = View.generateViewId()
        val teacherViewId = View.generateViewId()
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val type = v.find<View>(typeViewId)
        val time = v.find<TextView>(timeViewId)
        val auditory = v.find<TextView>(auditoryViewId)
        val discipline = v.find<TextView>(disciplineViewId)
        val teacher = v.find<TextView>(teacherViewId)
    }

    private val lessons = ArrayList<Lesson>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = parent.context.linearLayout {
            lparams {
                orientation = LinearLayout.HORIZONTAL
                width = matchParent
                margin = dip(1)
            }

            view { id = typeViewId }.lparams {
                width = dip(4)
                height = matchParent
                rightMargin = dip(4)
            }

            verticalLayout {
                relativeLayout {
                    textView { id = timeViewId }.lparams {
                        leftOf(auditoryViewId)
                        alignParentLeft()
                    }
                    textView { id = auditoryViewId }.lparams { alignParentRight() }
                }.lparams { width = matchParent }

                textView { id = disciplineViewId }
                textView { id = teacherViewId }
            }.lparams { width = matchParent }
        }

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val lesson = lessons[position]
        with(holder) {
            type.backgroundResource = when (lesson.type) {
                LessonType.PRACTICE -> R.color.practice_color
                LessonType.LECTURE -> R.color.lecture_color
                LessonType.LABORATORY -> R.color.laboratory_color
            }
            time.text = lesson.time
            auditory.text = lesson.auditory
            discipline.text = lesson.discipline
            teacher.text = lesson.teacher

            auditory.visibility = if (lesson.auditory == null) View.GONE else View.VISIBLE
            teacher.visibility = if (lesson.teacher == null) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = lessons.size

    fun updateData(lessons: List<Lesson>) {
        with(this.lessons) {
            clear()
            addAll(lessons)
        }
        notifyDataSetChanged()
    }

}