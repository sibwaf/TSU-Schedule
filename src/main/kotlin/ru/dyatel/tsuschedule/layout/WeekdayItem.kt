package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.ADAPTER_WEEKDAY_ITEM_ID
import ru.dyatel.tsuschedule.NORMAL_WEEKDAY_ORDER
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.model.Lesson

class WeekdayItem<T : Lesson>(
        private val name: String, lessons: List<T>,
        viewProvider: (Context) -> LessonView<T>
) : AbstractItem<WeekdayItem<T>, WeekdayItem.ViewHolder<T>>() {

    private companion object {
        private val weekdayViewId = View.generateViewId()
        private val lessonRecyclerViewId = View.generateViewId()
    }

    private val adapter = ItemAdapter<LessonItem<T>>()
    private val fastAdapter: FastAdapter<LessonItem<T>> = FastAdapter.with(adapter)

    init {
        withIdentifier(NORMAL_WEEKDAY_ORDER.indexOf(name).toLong())
        adapter.set(lessons.map { LessonItem(it, viewProvider) })
    }

    class ViewHolder<T : Lesson>(view: View) : FastAdapter.ViewHolder<WeekdayItem<T>>(view) {
        private val weekdayView = view.find<TextView>(weekdayViewId)
        private val lessonRecyclerView = view.find<RecyclerView>(lessonRecyclerViewId)

        override fun bindView(item: WeekdayItem<T>, payloads: List<Any>) {
            weekdayView.text = item.name
            lessonRecyclerView.adapter = item.fastAdapter
        }

        override fun unbindView(item: WeekdayItem<T>) {
            weekdayView.text = null
            lessonRecyclerView.adapter = null
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
                        id = weekdayViewId
                        gravity = Gravity.CENTER

                        textColorResource = R.color.text_title_color
                    }
                }

                recyclerView {
                    id = lessonRecyclerViewId

                    lparams(width = matchParent)

                    layoutManager = LinearLayoutManager(ctx)
                    ViewCompat.setNestedScrollingEnabled(this, false)
                }
            }
        }
    }

    override fun getType() = ADAPTER_WEEKDAY_ITEM_ID

    override fun getViewHolder(view: View) = ViewHolder<T>(view)

    override fun getLayoutRes() = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other as WeekdayItem<*>).name == name
    }
}
