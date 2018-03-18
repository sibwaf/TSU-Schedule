package ru.dyatel.tsuschedule.parsing

import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.TeacherLesson

object TeacherScheduleParser : ScheduleParser<TeacherLesson>() {

    override fun parseSingle(e: Element, base: Lesson): TeacherLesson {
        val groups = e.getElementsByClass("teac").map { it.text().trim() }
        if (groups.isEmpty()) {
            throw ParsingException("Group list can't be empty")
        }

        return with(base) { TeacherLesson(parity, weekday, time, discipline, auditory, type, groups) }
    }

}
