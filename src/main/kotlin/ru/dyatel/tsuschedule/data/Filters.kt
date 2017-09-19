package ru.dyatel.tsuschedule.data

import android.content.Context
import ru.dyatel.tsuschedule.layout.FilterView
import ru.dyatel.tsuschedule.layout.SubgroupFilterView

abstract class Filter(var enabled: Boolean) {

    abstract fun apply(lesson: Lesson): Lesson?

}

// TODO: serialization/deserialization
abstract class PredefinedFilter(enabled: Boolean) : Filter(enabled) {

    abstract fun createView(context: Context): FilterView

}

class SubgroupFilter(enabled: Boolean, var subgroup: Int) : PredefinedFilter(enabled) {

    override fun createView(context: Context): FilterView =
            SubgroupFilterView(context).also { it.attachFilter(this) }

    override fun apply(lesson: Lesson) = lesson.takeIf { it.subgroup == null || it.subgroup == subgroup }

}
