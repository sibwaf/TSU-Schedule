package ru.dyatel.tsuschedule.parsing

import ru.dyatel.tsuschedule.util.Filter

class SubgroupFilter(subgroup: Int) : Filter<Lesson> {

    private val subgroup: Int;

    init {
        this.subgroup = subgroup;
    }

    override fun accept(obj: Lesson?): Boolean =
            obj!!.subgroup == 0 || obj.subgroup == subgroup

}
