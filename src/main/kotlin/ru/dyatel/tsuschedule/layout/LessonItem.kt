package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import ru.dyatel.tsuschedule.ADAPTER_LESSON_ITEM_ID
import ru.dyatel.tsuschedule.model.Lesson

class LessonItem<T : Lesson>(
        private val lesson: T,
        private val viewProvider: (Context) -> LessonView<T>
) : AbstractItem<LessonItem<T>, LessonItem.ViewHolder<T>>() {

    init {
        withIdentifier(lesson.hashCode().toLong())
    }

    class ViewHolder<T : Lesson>(private val view: LessonView<T>)
        : FastAdapter.ViewHolder<LessonItem<T>>(view) {
        override fun bindView(item: LessonItem<T>, payloads: List<Any>) = view.bind(item.lesson)

        override fun unbindView(item: LessonItem<T>) = view.unbind()
    }

    override fun createView(ctx: Context, parent: ViewGroup?) = viewProvider(ctx)

    override fun getType() = ADAPTER_LESSON_ITEM_ID

    @Suppress("unchecked_cast")
    override fun getViewHolder(view: View) = ViewHolder(view as LessonView<T>)

    override fun getLayoutRes() = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other as LessonItem<*>).lesson == lesson
    }
}
