package ru.dyatel.tsuschedule.model

import android.content.Context
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.layout.FilterView
import ru.dyatel.tsuschedule.layout.SubgroupFilterView

abstract class Filter(var enabled: Boolean) {

    abstract fun apply(lesson: GroupLesson): GroupLesson?

}

abstract class PredefinedFilter : Filter(false) {

    abstract fun createView(context: Context): FilterView

    abstract fun save(): Map<String, String>

    abstract fun load(data: Map<String, String>)

}

class CommonPracticeFilter : PredefinedFilter() {

    override fun createView(context: Context) = FilterView(context).also {
        it.setHeader(R.string.filter_common_practice)
        it.attachFilter(this)
    }

    override fun save() = emptyMap<String, String>()

    override fun load(data: Map<String, String>) = Unit

    override fun apply(lesson: GroupLesson): GroupLesson {
        return with(lesson) {
            val newSubgroup = subgroup.takeUnless { type == LessonType.PRACTICE }
            GroupLesson(parity, weekday, time, discipline, auditory, teacher, type, newSubgroup)
        }
    }

}

class SubgroupFilter : PredefinedFilter() {

    private companion object {
        const val SUBGROUP_KEY = "subgroup"
    }

    var subgroup = 1

    override fun createView(context: Context) = SubgroupFilterView(context).also { it.attachFilter(this) }

    override fun save() = mapOf(SUBGROUP_KEY to subgroup.toString())

    override fun load(data: Map<String, String>) {
        data[SUBGROUP_KEY]?.let { subgroup = it.toInt() }
    }

    override fun apply(lesson: GroupLesson) = lesson.takeIf { it.subgroup == null || it.subgroup == subgroup }

}
