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
import ru.dyatel.tsuschedule.utilities.hideIf
import java.util.ArrayList

class LessonListAdapter : RecyclerView.Adapter<LessonListAdapter.Holder>() {

    private companion object {
        val typeMarkerViewId = View.generateViewId()
        val timeViewId = View.generateViewId()
        val auditoryViewId = View.generateViewId()
        val disciplineViewId = View.generateViewId()
        val teacherViewId = View.generateViewId()
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val typeMarker = v.find<View>(typeMarkerViewId)
        val time = v.find<TextView>(timeViewId)
        val auditory = v.find<TextView>(auditoryViewId)
        val discipline = v.find<TextView>(disciplineViewId)
        val teacher = v.find<TextView>(teacherViewId)
    }

    private val lessons = ArrayList<Lesson>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = parent.context.linearLayout {
            lparams(width = matchParent) {
                orientation = LinearLayout.HORIZONTAL
                margin = dip(2)
            }

            view { id = typeMarkerViewId }.lparams(width = dip(4), height = matchParent) {
                rightMargin = dip(4)
            }

            verticalLayout {
                lparams(width = matchParent)

                relativeLayout {
                    lparams(width = matchParent)

                    textView { id = timeViewId }.lparams {
                        leftOf(auditoryViewId)
                        alignParentLeft()
                    }
                    textView { id = auditoryViewId }.lparams {
                        alignParentRight()
                    }
                }

                textView { id = disciplineViewId }
                textView { id = teacherViewId }
            }
        }

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val lesson = lessons[position]
        with(holder) {
            val context = itemView.context

            val typeText = when (lesson.type) {
                LessonType.PRACTICE -> R.string.lesson_type_practice
                LessonType.LECTURE -> R.string.lesson_type_lecture
                LessonType.LABORATORY -> R.string.lesson_type_laboratory
                LessonType.UNKNOWN -> null
            }?.let { context.getString(it) }

            typeMarker.backgroundResource = when (lesson.type) {
                LessonType.PRACTICE -> R.color.practice_color
                LessonType.LECTURE -> R.color.lecture_color
                LessonType.LABORATORY -> R.color.laboratory_color
                LessonType.UNKNOWN -> R.color.unknown_color
            }
            time.text = lesson.time
            auditory.text = lesson.auditory
            discipline.text = typeText?.let { "${lesson.discipline} ($typeText)" } ?: lesson.discipline
            teacher.text = lesson.teacher

            auditory.hideIf { lesson.auditory == null }
            teacher.hideIf { lesson.teacher == null }
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