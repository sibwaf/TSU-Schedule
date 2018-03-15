package ru.dyatel.tsuschedule.parsing

import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.data.BaseLesson
import ru.dyatel.tsuschedule.data.TeacherLesson

object TeacherScheduleParser : BaseParser<TeacherLesson>() {

    override fun parseSingle(e: Element, base: BaseLesson): TeacherLesson {
        val groups = e.getElementsByClass("teac").map { it.text().trim() }
        if (groups.isEmpty()) {
            throw ParsingException("Group list can't be empty")
        }

        return with(base) { TeacherLesson(parity, weekday, time, discipline, auditory, type, groups) }
    }

}
