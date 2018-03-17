package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import ru.dyatel.tsuschedule.ADAPTER_LESSON_ITEM_ID
import ru.dyatel.tsuschedule.data.BaseLesson

class LessonItem<T : BaseLesson>(
        private val lesson: T,
        private val viewProvider: (Context) -> BaseLessonView<T>
) : AbstractItem<LessonItem<T>, LessonItem.ViewHolder<T>>() {

    init {
        withIdentifier(lesson.hashCode().toLong())
    }

    class ViewHolder<T : BaseLesson>(private val view: BaseLessonView<T>)
        : FastAdapter.ViewHolder<LessonItem<T>>(view) {
        override fun bindView(item: LessonItem<T>, payloads: List<Any>) = view.bind(item.lesson)

        override fun unbindView(item: LessonItem<T>) = view.unbind()
    }

    override fun createView(ctx: Context, parent: ViewGroup?) = viewProvider(ctx)

    override fun getType() = ADAPTER_LESSON_ITEM_ID

    @Suppress("unchecked_cast")
    override fun getViewHolder(view: View) = ViewHolder(view as BaseLessonView<T>)

    override fun getLayoutRes() = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other as LessonItem<*>).lesson == lesson
    }
}
