package ru.dyatel.tsuschedule.parsing

import hirondelle.date4j.DateTime
import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.model.Exam

object ExamScheduleParser : ParserBase() {

    private val DATETIME_PATTERN = Regex("^(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+)$") // 11.06.18 09:00

    fun parse(element: Element): Set<Exam> {
        return element.children()
                .filter { it.tagName() == "div" }
                .map { Exam(parseDatetime(it), parseDiscipline(it), parseAuditory(it), parseTeacher(it)) }
                .toSet()
    }

    private fun parseDatetime(e: Element): DateTime {
        val text = e.getElementsByClass("time").requireSingle().text().trim()

        val groups = DATETIME_PATTERN.matchEntire(text)
                ?.groupValues
                ?: throw ParsingException("Can't parse datetime from $text")

        return DateTime(
                groups[3].toInt(), // Year
                groups[2].toInt(), // Month
                groups[1].toInt(), // Day
                groups[4].toInt(), // Hour
                groups[5].toInt(), // Minute
                0, 0)
    }

    private fun parseDiscipline(e: Element) =
            e.getElementsByClass("disc").requireSingle().text().trim().removeSuffix(",")

    private fun parseAuditory(e: Element) =
            e.getElementsByClass("aud").requireSingle().text().trim()

    private fun parseTeacher(e: Element) =
            e.getElementsByClass("teac").requireSingle().text().trim()

}
