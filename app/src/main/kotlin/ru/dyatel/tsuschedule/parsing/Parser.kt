package ru.dyatel.tsuschedule.parsing

import org.jsoup.Jsoup
import java.util.HashSet

class Parser {

    private companion object {
        val WEEKDAY_PATTERN = Regex("\\b[А-Яа-я]+\\b")
    }

    private val connection = Jsoup.connect("http://schedule.tsu.tula.ru/")

    fun setTimeout(timeout: Int) {
        connection.timeout(timeout)
    }

    fun getLessons(group: String): Set<Lesson> {
        val response = connection.data("group", group).get()
        val result = response.getElementById("results")

        if (result.childNodeSize() <= 1) throw BadGroupException()

        val lessons = HashSet<Lesson>()
        var currentWeekday: String? = null

        result.children()
                .map { it.child(0).child(0).children().last() } // Ignore the padding row
                .forEach {
                    val builder = LessonBuilder()

                    val timeText = it.getElementsByClass("time").requireSingle().text().trim()
                    val weekday = WEEKDAY_PATTERN.find(timeText)?.value ?: currentWeekday
                    weekday ?: throw ParsingException("Can't find weekday of the lesson")
                    currentWeekday = weekday

                    builder.setWeekday(weekday)
                    builder.parseParity(it.getElementsByClass("parity").requireSingle().text())
                    builder.parseTime(timeText)

                    builder.parseDescription(it.getElementsByClass("disc").requireSingle().text())
                    it.getElementsByClass("aud").takeIf { it.isNotEmpty() }?.let {
                        builder.parseAuditory(it.requireSingle().text())
                    }
                    it.getElementsByClass("teac").takeIf { it.isNotEmpty() }?.let {
                        builder.parseTeacher(it.requireSingle().text())
                    }

                    lessons += builder.build()
                }

        return lessons
    }

    private fun <T> Collection<T>.requireSingle() = singleOrNull() ?: throw ParsingException()

}

private class LessonBuilder {

    private companion object {
        val BLANK_PARENTHESES_PATTERN = Regex("\\(\\s*\\)")

        val TIME_PATTERN = Regex("\\d{2}:\\d{2}-\\d{2}:\\d{2}")

        val SUBGROUP_PATTERN = Regex("\\((\\d) ?п/?гр?\\)")

        val TYPE_MAPPING = mapOf(
                "Пр" to LessonType.PRACTICE,
                "Практ" to LessonType.PRACTICE,
                "Л" to LessonType.LECTURE,
                "Лаб" to LessonType.LABORATORY
        )
        val TYPE_PATTERN = TYPE_MAPPING.keys.joinToString("|", "\\((", ")\\.?\\)").toRegex()
    }

    private var parity: Parity? = null

    private var weekday: String? = null
    private var time: String? = null

    private var discipline: String? = null
    private var auditory = ""
    private var teacher = ""

    private var type: LessonType? = null
    private var subgroup: Int = 0

    fun parseParity(text: String) {
        parity = when (text.trim()) {
            "н/н" -> Parity.ODD
            "ч/н" -> Parity.EVEN
            else -> throw ParsingException("Can't determine parity from <$text>")
        }
    }

    fun setWeekday(text: String) {
        weekday = text
    }

    fun parseTime(text: String) {
        time = TIME_PATTERN.find(text.trim())?.value ?:
                throw ParsingException("Can't parse lesson's time from <$text>")
    }

    fun parseDescription(text: String) {
        text.replace(BLANK_PARENTHESES_PATTERN, "").let {
            SUBGROUP_PATTERN.find(it)?.let { match ->
                subgroup = match.groupValues[1].toInt()
                it.removeRange(match.range)
            } ?: it
        }.let {
            TYPE_PATTERN.find(it)?.let { match ->
                type = TYPE_MAPPING[match.groupValues[1]]!!
                it.removeRange(match.range)
            } ?: throw ParsingException("Can't determine lesson type from <$text>")
        }.trim().removeSuffix(",").trim().let { discipline = it }
    }

    fun parseAuditory(text: String) {
        auditory = text.trim()
    }

    fun parseTeacher(text: String) {
        teacher = text.trim()
    }

    fun build() = Lesson(parity!!, weekday!!, time!!, discipline!!, auditory, teacher, type!!, subgroup)

}
