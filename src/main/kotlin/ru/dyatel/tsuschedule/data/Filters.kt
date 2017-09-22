package ru.dyatel.tsuschedule.data

import android.content.Context
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.layout.FilterView
import ru.dyatel.tsuschedule.layout.SubgroupFilterView

abstract class Filter(var enabled: Boolean) {

    abstract fun apply(lesson: Lesson): Lesson?

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

    override fun apply(lesson: Lesson): Lesson? = Lesson(
            lesson.parity, lesson.weekday, lesson.time,
            lesson.discipline, lesson.auditory, lesson.teacher, lesson.type,
            lesson.subgroup.takeUnless { lesson.type == LessonType.PRACTICE }
    )

}

class SubgroupFilter : PredefinedFilter() {

    var subgroup = 1

    private companion object {
        const val SUBGROUP_KEY = "subgroup"
    }

    override fun createView(context: Context) = SubgroupFilterView(context).also { it.attachFilter(this) }

    override fun save() = mapOf(SUBGROUP_KEY to subgroup.toString())

    override fun load(data: Map<String, String>) {
        data[SUBGROUP_KEY]?.toInt()?.let { subgroup = it }
    }

    override fun apply(lesson: Lesson) = lesson.takeIf { it.subgroup == null || it.subgroup == subgroup }

}
