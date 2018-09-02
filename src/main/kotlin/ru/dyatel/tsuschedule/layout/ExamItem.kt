package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import hirondelle.date4j.DateTime
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.horizontalMargin
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.verticalMargin
import org.jetbrains.anko.view
import ru.dyatel.tsuschedule.ADAPTER_EXAM_ITEM_ID
import ru.dyatel.tsuschedule.NORMAL_WEEKDAY_ORDER
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.model.Exam
import ru.dyatel.tsuschedule.utilities.convertWeekday
import ru.dyatel.tsuschedule.utilities.hideIf

class ExamItem(val exam: Exam) : AbstractItem<ExamItem, ExamItem.ViewHolder>() {

    private companion object {
        val disciplineViewId = View.generateViewId()
        val teacherViewId = View.generateViewId()

        val consultationContainerId = View.generateViewId()

        val consultationDatetimeViewId = View.generateViewId()
        val consultationAuditoryViewId = View.generateViewId()

        val examDatetimeViewId = View.generateViewId()
        val examAuditoryViewId = View.generateViewId()
    }

    init {
        withIdentifier(exam.hashCode().toLong())
    }


    class ViewHolder(view: View) : FastAdapter.ViewHolder<ExamItem>(view) {

        private companion object {
            const val DATETIME_FORMAT = "DD.MM.YYYY, hh:mm"
        }

        private val context = view.context

        private val disciplineView = view.find<TextView>(disciplineViewId)
        private val teacherView = view.find<TextView>(teacherViewId)

        private val consultationContainer = view.find<View>(consultationContainerId)

        private val consultationDatetimeView = view.find<TextView>(consultationDatetimeViewId)
        private val consultationAuditoryView = view.find<TextView>(consultationAuditoryViewId)

        private val examDatetimeView = view.find<TextView>(examDatetimeViewId)
        private val examAuditoryView = view.find<TextView>(examAuditoryViewId)

        override fun bindView(item: ExamItem, payloads: List<Any>) {
            with(item.exam) {
                disciplineView.text = discipline
                teacherView.text = teacher

                if (consultationDatetime != null) {
                    consultationDatetimeView.text = context.getString(
                            R.string.label_exam_datetime,
                            formatDatetime(consultationDatetime)
                    )
                    consultationAuditoryView.text = context.getString(
                            R.string.label_exam_auditory,
                            consultationAuditory!!
                    )
                }

                consultationContainer.hideIf { consultationDatetime == null }

                examDatetimeView.text = context.getString(R.string.label_exam_datetime, formatDatetime(examDatetime))
                examAuditoryView.text = context.getString(R.string.label_exam_auditory, examAuditory)
            }
        }

        override fun unbindView(item: ExamItem) {
            disciplineView.text = null
            teacherView.text = null
            consultationDatetimeView.text = null
            consultationAuditoryView.text = null
            examDatetimeView.text = null
            examAuditoryView.text = null
        }

        private fun formatDatetime(datetime: DateTime): String {
            val weekday = convertWeekday(datetime.weekDay)
            return "${datetime.format(DATETIME_FORMAT)} (${NORMAL_WEEKDAY_ORDER[weekday - 1]})"
        }
    }

    override fun createView(ctx: Context, parent: ViewGroup?): View {
        return ctx.cardView {
            lparams(width = matchParent) {
                leftMargin = DIM_CARD_HORIZONTAL_MARGIN
                rightMargin = DIM_CARD_HORIZONTAL_MARGIN
                topMargin = DIM_CARD_VERTICAL_MARGIN
                bottomMargin = DIM_CARD_VERTICAL_MARGIN
            }

            cardElevation = DIM_ELEVATION_F
            radius = DIM_CARD_RADIUS_F

            verticalLayout(R.style.WeekdayTheme) {
                lparams(width = matchParent) {
                    padding = DIM_CARD_PADDING
                }

                frameLayout {
                    lparams(width = matchParent) {
                        bottomMargin = DIM_CARD_PADDING
                        padding = DIM_MEDIUM
                    }

                    backgroundColorResource = R.color.primary_color

                    textView {
                        id = disciplineViewId
                        gravity = Gravity.CENTER

                        textColorResource = R.color.text_title_color
                    }
                }

                verticalLayout {
                    lparams(width = matchParent)

                    textView {
                        id = teacherViewId
                        gravity = Gravity.CENTER_HORIZONTAL
                    }.lparams(width = matchParent) {
                        bottomMargin = DIM_LARGE
                    }

                    linearLayout {
                        lparams(width = matchParent) {
                            horizontalMargin = DIM_SMALL
                            verticalMargin = DIM_MEDIUM
                        }

                        id = consultationContainerId

                        view {
                            backgroundResource = R.color.consultation_color
                        }.lparams(width = DIM_MEDIUM, height = matchParent) {
                            rightMargin = DIM_CARD_PADDING
                        }

                        verticalLayout {
                            lparams(width = matchParent)

                            textView { textResource = R.string.label_consultation }
                            textView { id = consultationDatetimeViewId }
                            textView { id = consultationAuditoryViewId }
                        }
                    }

                    linearLayout {
                        lparams(width = matchParent) {
                            horizontalMargin = DIM_SMALL
                            verticalMargin = DIM_MEDIUM
                        }

                        view {
                            backgroundResource = R.color.exam_color
                        }.lparams(width = DIM_MEDIUM, height = matchParent) {
                            rightMargin = DIM_CARD_PADDING
                        }

                        verticalLayout {
                            lparams(width = matchParent)

                            textView { textResource = R.string.label_exam }
                            textView { id = examDatetimeViewId }
                            textView { id = examAuditoryViewId }
                        }
                    }
                }
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
