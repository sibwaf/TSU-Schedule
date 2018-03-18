package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.View
import android.widget.TextView
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.model.GroupLesson
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.LessonType
import ru.dyatel.tsuschedule.model.TeacherLesson
import ru.dyatel.tsuschedule.utilities.hideIf

abstract class LessonView<in T : Lesson>(context: Context) : _LinearLayout(context) {

    private companion object {
        val timeViewId = View.generateViewId()
        val auditoryViewId = View.generateViewId()
        val disciplineViewId = View.generateViewId()
    }

    private val typeMarkerView: View
    private val timeView: TextView
    private val auditoryView: TextView
    private val disciplineView: TextView

    init {
        lparams(width = matchParent) {
            margin = dip(2)
        }

        typeMarkerView = view {
        }.lparams(width = dip(4), height = matchParent) {
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

            customize(this)
        }

        timeView = find(timeViewId)
        auditoryView = find(auditoryViewId)
        disciplineView = find(disciplineViewId)
    }

    protected abstract fun customize(container: _LinearLayout)

    open fun bind(lesson: T) {
        typeMarkerView.backgroundResource = when (lesson.type) {
            LessonType.PRACTICE -> R.color.practice_color
            LessonType.LECTURE -> R.color.lecture_color
            LessonType.LABORATORY -> R.color.laboratory_color
            LessonType.UNKNOWN -> R.color.unknown_color
        }

        val typeText = when (lesson.type) {
            LessonType.PRACTICE -> R.string.lesson_type_practice
            LessonType.LECTURE -> R.string.lesson_type_lecture
            LessonType.LABORATORY -> R.string.lesson_type_laboratory
            LessonType.UNKNOWN -> null
        }?.let { context.getString(it) }

        timeView.text = lesson.time
        auditoryView.apply {
            text = lesson.auditory
            hideIf { text == null }
        }
        disciplineView.text = typeText?.let { "${lesson.discipline} ($typeText)" } ?: lesson.discipline
    }

    open fun unbind() {
        timeView.text = null
        auditoryView.text = null
        disciplineView.text = null
    }

}

class GroupLessonView(context: Context) : LessonView<GroupLesson>(context) {

    private lateinit var teacherView: TextView

    override fun customize(container: _LinearLayout) {
        container.apply {
            teacherView = textView()
        }
    }

    override fun bind(lesson: GroupLesson) {
        super.bind(lesson)
        teacherView.apply {
            text = lesson.teacher
            hideIf { text == null }
        }
    }

    override fun unbind() {
        teacherView.text = null
        super.unbind()
    }

}

class TeacherLessonView(context: Context) : LessonView<TeacherLesson>(context) {

    private lateinit var groupView: TextView

    override fun customize(container: _LinearLayout) {
        container.apply {
            groupView = textView()
        }
    }

    override fun bind(lesson: TeacherLesson) {
        super.bind(lesson)
        groupView.apply {
            text = lesson.groups.joinToString(", ").takeUnless { it.isEmpty() }
            hideIf { text == null }
        }
    }

    override fun unbind() {
        groupView.text = null
        super.unbind()
    }

}
