package ru.dyatel.tsuschedule.parsing

import hirondelle.date4j.DateTime
import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.EmptyResultException
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.model.Exam

private data class Token(
        val type: TokenType?,
        val discipline: String,
        val datetime: DateTime,
        val auditory: String,
        val teacher: String
)

private enum class TokenType {
    CONSULTATION, EXAM
}

object ExamScheduleParser : ParserBase() {

    private val DATETIME_PATTERN = Regex("^(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+)$") // 11.06.18 09:00

    private val TYPE_MAPPING = mapOf(
            "конс" to TokenType.CONSULTATION,
            "э" to TokenType.EXAM
    )
    private val TYPE_MARKER_PATTERN = Regex("\\(\\s*([А-Яа-я]+)\\.?\\s*\\)")

    fun parse(element: Element): Set<Exam> {
        if (element.childNodeSize() < 1) {
            throw EmptyResultException()
        }

        return element.children()
                .filter { it.tagName() == "div" }
                .map { parseSingle(it) }
                .distinct()
                .groupBy { it.discipline }
                .flatMap { merge(it.value) }
                .toSet()
    }

    private fun parseSingle(e: Element): Token {
        var discipline = parseDiscipline(e)
        val type = TYPE_MARKER_PATTERN.findAll(discipline)
                .map { it to it.groupValues[1].toLowerCase() }
                .singleOrNull { TYPE_MAPPING.containsKey(it.second) }
                ?.let {
                    discipline = discipline.removeRange(it.first.range).trim()
                    TYPE_MAPPING[it.second]!!
                }

        val datetime = parseDatetime(e)
        val auditory = parseAuditory(e)
        val teacher = parseTeacher(e)

        return Token(type, discipline, datetime, auditory, teacher)
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

    private fun merge(tokens: Collection<Token>): Collection<Exam> {
        val result = mutableListOf<Exam>()

        val source = tokens.toMutableList()
        val consultationToken = source.singleOrNull { it.type == TokenType.CONSULTATION }
        val examToken = source.singleOrNull { it.type == TokenType.EXAM }

        if (consultationToken != null && examToken != null) {
            var teacher = examToken.teacher
            if (consultationToken.teacher != examToken.teacher) {
                teacher = "${consultationToken.teacher}, $teacher"
            }

            result += Exam(
                    examToken.discipline,
                    consultationToken.datetime,
                    examToken.datetime,
                    consultationToken.auditory,
                    examToken.auditory,
                    teacher)

            source -= consultationToken
            source -= examToken
        }

        for (token in source) {
            result += Exam(
                    token.discipline,
                    null,
                    token.datetime,
                    null,
                    token.auditory,
                    token.teacher)
        }

        return result
    }

}
