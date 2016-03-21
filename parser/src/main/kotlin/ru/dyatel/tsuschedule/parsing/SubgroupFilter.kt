package ru.dyatel.tsuschedule.parsing

import ru.dyatel.tsuschedule.util.Filter

class SubgroupFilter(private val subgroup: Int) : Filter<Lesson> {

    override fun accept(obj: Lesson): Boolean =
            obj.subgroup == 0 || obj.subgroup == subgroup

}
