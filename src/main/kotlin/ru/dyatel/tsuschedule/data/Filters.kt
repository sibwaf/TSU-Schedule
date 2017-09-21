package ru.dyatel.tsuschedule.data

import android.content.Context
import ru.dyatel.tsuschedule.layout.FilterView
import ru.dyatel.tsuschedule.layout.SubgroupFilterView

enum class FilterType {
    SUBGROUP
}

abstract class Filter(var enabled: Boolean) {

    abstract fun apply(lesson: Lesson): Lesson?

    abstract fun getType(): FilterType

}

abstract class PredefinedFilter : Filter(false) {

    abstract fun createView(context: Context): FilterView

    abstract fun save(): Map<String, String>

    abstract fun load(data: Map<String, String>)

}

class SubgroupFilter : PredefinedFilter() {

    var subgroup = 1

    companion object {
        const val SUBGROUP_KEY = "subgroup"
    }

    override fun getType() = FilterType.SUBGROUP

    override fun createView(context: Context) = SubgroupFilterView(context).also { it.attachFilter(this) }

    override fun save() = mapOf(SUBGROUP_KEY to subgroup.toString())

    override fun load(data: Map<String, String>) {
        data[SUBGROUP_KEY]?.toInt()?.let { subgroup = it }
    }

    override fun apply(lesson: Lesson) = lesson.takeIf { it.subgroup == null || it.subgroup == subgroup }

}
