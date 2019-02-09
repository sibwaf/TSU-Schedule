package ru.dyatel.tsuschedule.parsing

import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.LessonType
import ru.dyatel.tsuschedule.model.Parity

private class WeekdayToken(weekdayRow: Element) {

    private companion object {
        val PARITY_MAPPING = mapOf(
                "н/н" to Parity.ODD,
                "нечётная неделя" to Parity.ODD,
                "ч/н" to Parity.EVEN,
                "чётная неделя" to Parity.EVEN
        )
        val WEEKDAY_PATTERN = PARITY_MAPPING.keys
                .joinToString("|", "\\b([А-Яа-я]+)\\b\\s*\\((", ")\\)")
                .toRegex(RegexOption.IGNORE_CASE)
    }

    private val lessons = mutableListOf<Element>()

    val weekday: String
    val parity: Parity

    init {
        val weekdayText = weekdayRow.select(".time").text().trim()
        val match = WEEKDAY_PATTERN.find(weekdayText)
                ?: throw ParsingException("Failed to parse weekday from <$weekdayText>")

        weekday = match.groupValues[1]
        parity = match.groupValues[2].toLowerCase()
                .let { parity ->
                    PARITY_MAPPING[parity] ?: throw ParsingException("Failed to parse parity from <$parity>")
                }
    }

    fun list(): List<Element> = lessons

    fun add(element: Element) {
        lessons += element
    }

}

abstract class ScheduleParser<out T : Lesson> : ParserBase() {

    private companion object {
        val SURPLUS_SPACING_PATTERN = Regex("\\s{2,}")
        val SPACES_BEFORE_COMMA_PATTERN = Regex("\\s+,")
        val LEFT_PARENTHESIS_SPACING_PATTERN = Regex("\\(\\s+")
        val RIGHT_PARENTHESIS_SPACING_PATTERN = Regex("\\s+\\)")
        val BLANK_PARENTHESES_PATTERN = Regex("\\(\\s*\\)")

        val TIME_PATTERN = Regex("\\d{2}:\\d{2}-\\d{2}:\\d{2}")

        val TYPE_MAPPING = mapOf(
                "пр" to LessonType.PRACTICE,
                "практ" to LessonType.PRACTICE,
                "практические занятия" to LessonType.PRACTICE,
                "л" to LessonType.LECTURE,
                "лекционные занятия" to LessonType.LECTURE,
                "лаб" to LessonType.LABORATORY,
                "лабораторные работы" to LessonType.LABORATORY
        )
        val TYPE_PATTERN = TYPE_MAPPING.keys
                .joinToString("|", "\\((", ")\\.?\\)")
                .toRegex(RegexOption.IGNORE_CASE)
    }

    fun parse(element: Element): Set<T> {
        val rows = element.select("tr")

        val weekdays = mutableListOf<WeekdayToken>()
        var currentWeekday: WeekdayToken? = null

        for (row in rows) {
            if (row.children().size == 1) {
                currentWeekday = WeekdayToken(row)
                weekdays += currentWeekday
                continue
            }

            currentWeekday ?: throw ParsingException("Weekday row was not found")
            currentWeekday.add(row)
        }

        return weekdays
                .flatMap { token ->
                    token.list().map {
                        val time = parseTime(it)
                        val auditory = parseAuditory(it)
                        val (type, discipline) = parseDiscipline(it)

                        parseSingle(it, Lesson(token.parity, token.weekday, time, discipline, auditory, type))
                    }
                }
                .toSet()
    }

    private fun parseTime(e: Element): String {
        val text = e.getElementsByClass("time").requireSingle().text().trim()
        return TIME_PATTERN.find(text)?.value ?: throw ParsingException("Can't parse time from <$text>")
    }

    private fun parseAuditory(e: Element): String? {
        val text = e.getElementsByClass("aud").requireSingleOrNull()?.text()?.trim()
        return text?.takeUnless { it.isEmpty() }
    }

    private fun parseDiscipline(e: Element): Pair<LessonType, String> {
        var text = e.getElementsByClass("disc").requireSingle().text().clean()

        val typeMatch = TYPE_PATTERN.find(text)
        val type: LessonType?

        if (typeMatch != null) {
            type = TYPE_MAPPING[typeMatch.groupValues[1].toLowerCase()]!!
            text = text.removeRange(typeMatch.range).clean()
        } else {
            type = LessonType.UNKNOWN
        }

        return type to text.trim()
    }

    protected abstract fun parseSingle(e: Element, base: Lesson): T

    protected fun String.clean(): String {
        return replace(BLANK_PARENTHESES_PATTERN, "")
                .replace(SURPLUS_SPACING_PATTERN, " ")
                .replace(SPACES_BEFORE_COMMA_PATTERN, ",")
                .replace(LEFT_PARENTHESIS_SPACING_PATTERN, "(")
                .replace(RIGHT_PARENTHESIS_SPACING_PATTERN, ")")
                .trim()
                .removeSuffix(",")
    }

}
