package ru.dyatel.tsuschedule.parsing

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.EmptyResultException
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.LessonType
import ru.dyatel.tsuschedule.model.Parity
import ru.dyatel.tsuschedule.model.RawSchedule
import java.util.HashSet

abstract class ScheduleParser<out T : Lesson> {

    private companion object {
        val WEEKDAY_PATTERN = Regex("\\b[А-Яа-я]+\\b")
        val TIME_PATTERN = Regex("\\d{2}:\\d{2}-\\d{2}:\\d{2}")
        val BLANK_PARENTHESES_PATTERN = Regex("\\(\\s*\\)")

        val TYPE_MAPPING = mapOf(
                "Пр" to LessonType.PRACTICE,
                "Практ" to LessonType.PRACTICE,
                "Л" to LessonType.LECTURE,
                "Лаб" to LessonType.LABORATORY
        )
        val TYPE_PATTERN = TYPE_MAPPING.keys.joinToString("|", "\\((", ")\\.?\\)").toRegex()
    }

    fun parse(schedule: RawSchedule): Set<T> {
        val element = Jsoup.parse(schedule.data).body().child(0)

        if (element.childNodeSize() <= 1) {
            throw EmptyResultException()
        }

        val lessons = HashSet<T>()
        var currentWeekday: String? = null

        element.children()
                .filter { !it.hasClass("screenonly") }
                .map { it.child(0).child(0).children().last() } // Ignore the padding row
                .forEach {
                    val timeText = it.getElementsByClass("time").requireSingle().text().trim()
                    val weekday = WEEKDAY_PATTERN.find(timeText)?.value ?: currentWeekday
                    currentWeekday = weekday ?: throw ParsingException("Can't find weekday of the lesson")

                    val parity = parseParity(it)
                    val time = parseTime(timeText)
                    val auditory = parseAuditory(it)
                    val (type, discipline) = parseDiscipline(it)

                    lessons += parseSingle(it, Lesson(parity, weekday, time, discipline, auditory, type))
                }

        return lessons
    }

    private fun parseParity(e: Element): Parity {
        val text = e.getElementsByClass("parity").requireSingle().text().trim()
        return when (text) {
            "н/н" -> Parity.ODD
            "ч/н" -> Parity.EVEN
            else -> throw ParsingException("Can't determine parity from <$text>")
        }
    }

    private fun parseTime(text: String): String {
        return TIME_PATTERN.find(text)?.value ?: throw ParsingException("Can't parse time from <$text>")
    }

    private fun parseAuditory(e: Element): String? {
        val text = e.getElementsByClass("aud").requireSingleOrNull()?.text()?.trim()
        return text?.takeUnless { it.isEmpty() }
    }

    private fun parseDiscipline(e: Element): Pair<LessonType, String> {
        var text = e.getElementsByClass("disc").requireSingle().text().trim()
                .removeSuffix(",")
                .replace(BLANK_PARENTHESES_PATTERN, "")

        val typeMatch = TYPE_PATTERN.find(text)
        val type: LessonType?

        if (typeMatch != null) {
            type = TYPE_MAPPING[typeMatch.groupValues[1]]!!
            text = text.removeRange(typeMatch.range)
        } else {
            type = LessonType.UNKNOWN
        }

        return type to text.trim()
    }

    protected abstract fun parseSingle(e: Element, base: Lesson): T

    protected fun <T> Collection<T>.requireSingle() = singleOrNull() ?: throw ParsingException()

    protected fun <T> Collection<T>.requireSingleOrNull() = if (isEmpty()) null else requireSingle()

}
