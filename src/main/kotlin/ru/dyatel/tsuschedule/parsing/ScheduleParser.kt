package ru.dyatel.tsuschedule.parsing

import org.jsoup.Connection
import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.data.BaseLesson
import ru.dyatel.tsuschedule.data.Lesson

class ScheduleParser(private val group: String) : BaseParser<Lesson>() {

    private companion object {
        val SUBGROUP_PATTERN = Regex("\\(\\s*(\\d) ?п/?гр?\\s*\\)")
    }

    override fun prepare(connection: Connection) = connection.data("group", group)!!

    override fun parse(e: Element, base: BaseLesson): Lesson {
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
