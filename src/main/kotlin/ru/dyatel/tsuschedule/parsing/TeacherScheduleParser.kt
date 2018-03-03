package ru.dyatel.tsuschedule.parsing

import org.jsoup.Connection
import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.data.BaseLesson
import ru.dyatel.tsuschedule.data.TeacherLesson

class TeacherScheduleParser(private val teacher: String) : BaseParser<TeacherLesson>() {

    override fun prepare(connection: Connection) = connection.data("teacher", teacher)!!

    override fun parse(e: Element, base: BaseLesson): TeacherLesson {
        val groups = e.getElementsByClass("teac").map { it.text().trim() }
        if (groups.isEmpty()) {
            throw ParsingException("Group list can't be empty")
        }

        return with(base) { TeacherLesson(parity, weekday, time, discipline, auditory, type, groups) }
    }

}
