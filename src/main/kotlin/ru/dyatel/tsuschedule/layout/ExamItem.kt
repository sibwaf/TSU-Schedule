package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.ADAPTER_EXAM_ITEM_ID
import ru.dyatel.tsuschedule.model.Exam

class ExamItem(val exam: Exam) : AbstractItem<ExamItem, ExamItem.ViewHolder>() {

    private companion object {
        val datetimeViewId = View.generateViewId()
        val disciplineViewId = View.generateViewId()
        val auditoryViewId = View.generateViewId()
        val teacherViewId = View.generateViewId()
    }

    init {
        withIdentifier(exam.hashCode().toLong())
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<ExamItem>(view) {
        private val datetimeView = view.find<TextView>(datetimeViewId)
        private val disciplineView = view.find<TextView>(disciplineViewId)
        private val auditoryView = view.find<TextView>(auditoryViewId)
        private val teacherView = view.find<TextView>(teacherViewId)

        override fun bindView(item: ExamItem, payloads: List<Any>) {
            // TODO: показывать день недели
            with(item.exam) {
                datetimeView.text = datetime.format("DD.MM.YYYY, hh:mm")
                disciplineView.text = discipline
                auditoryView.text = auditory
                teacherView.text = teacher
            }
        }

        override fun unbindView(item: ExamItem) {
            datetimeView.text = null
            disciplineView.text = null
            auditoryView.text = null
            teacherView.text = null
        }
    }

    override fun createView(ctx: Context, parent: ViewGroup?): View {
        // TODO: дизайн
        return ctx.cardView {
            lparams(width = matchParent) {
                margin = dip(4)
            }

            cardElevation = DIP_ELEVATION_F
            radius = DIP_CARD_RADIUS_F

            verticalLayout {
                textView {
                    id = disciplineViewId
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                textView { id = teacherViewId }
                textView { id = datetimeViewId }
                textView { id = auditoryViewId }
            }
        }
    }

    override fun getType() = ADAPTER_EXAM_ITEM_ID

    override fun getViewHolder(view: View) = ViewHolder(view)

    override fun getLayoutRes() = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other as ExamItem).exam == exam
    }
}
