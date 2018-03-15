package ru.dyatel.tsuschedule.parsing

import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.data.BaseLesson
import ru.dyatel.tsuschedule.data.Lesson

object GroupScheduleParser : BaseParser<Lesson>() {

    private val SUBGROUP_PATTERN = Regex("\\(\\s*(\\d) ?п/?гр?\\s*\\)")

    override fun parseSingle(e: Element, base: BaseLesson): Lesson {
        val teacher = e.getElementsByClass("teac").requireSingleOrNull()
                ?.text()?.trim()?.takeUnless { it.isEmpty() }

        val subgroup: Int?
        val discipline: String

        val match = SUBGROUP_PATTERN.find(base.discipline)
        if (match != null) {
            subgroup = match.groupValues[1].toInt()
            discipline = base.discipline.removeRange(match.range).trim()
        } else {
            subgroup = null
            discipline = base.discipline
        }

        return with(base) { Lesson(parity, weekday, time, discipline, auditory, teacher, type, subgroup) }
    }

}
