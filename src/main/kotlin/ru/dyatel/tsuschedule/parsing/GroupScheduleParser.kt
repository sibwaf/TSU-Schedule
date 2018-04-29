package ru.dyatel.tsuschedule.parsing

import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.model.GroupLesson
import ru.dyatel.tsuschedule.model.Lesson

object GroupScheduleParser : ScheduleParser<GroupLesson>() {

    private val SUBGROUP_PATTERN = Regex("(\\d) ?п/?гр?,?")

    override fun parseSingle(e: Element, base: Lesson): GroupLesson {
        val teacher = e.getElementsByClass("teac").requireSingleOrNull()
                ?.text()?.trim()?.takeUnless { it.isEmpty() }

        val subgroup: Int?
        val discipline: String

        val match = SUBGROUP_PATTERN.find(base.discipline)
        if (match != null) {
            subgroup = match.groupValues[1].toInt()
            discipline = base.discipline.removeRange(match.range).clean()
        } else {
            subgroup = null
            discipline = base.discipline
        }

        return with(base) { GroupLesson(parity, weekday, time, discipline, auditory, teacher, type, subgroup) }
    }

}
