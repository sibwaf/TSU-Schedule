package ru.dyatel.tsuschedule.parsing

import org.jsoup.Jsoup
import java.util.HashSet
import java.util.regex.Pattern

private const val PRACTICE = "Пр"
private const val LECTURE = "Л"
private const val LABORATORY = "Лаб"

private const val ODD_PARITY = "н/н"
private const val EVEN_PARITY = "ч/н"

private val TIME_PATTERN = Pattern.compile("^(?:(\\S+?) )?(\\S+?)$")
private val DISCIPLINE_PATTERN = Pattern.compile(
        "^(.+? \\(($PRACTICE|$LECTURE|$LABORATORY)\\.?\\))(?: \\((\\d) ?п/?гр?\\))?$"
)

class Parser {

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
        for (it in result.children()) {
            val rows = it.child(0).child(0).children()
            val data = if (rows.size == 1) rows[0] else rows[1] // Ignore the padding row if it is present

            val timeString = data.getElementsByClass("time").text().trim()
            val timeMatcher = TIME_PATTERN.matcher(timeString)
            if (!timeMatcher.matches()) throw ParsingException("Can't parse time string: $timeString")

            val weekday = timeMatcher.group(1)
            if (weekday != null) currentWeekday = weekday
            if (currentWeekday == null) throw ParsingException("Can't find weekday of the lesson")

            val time = timeMatcher.group(2)

            val parity = extractParity(data.getElementsByClass("parity").text().trim())

            val disciplineString = data.getElementsByClass("disc").text().trim().removeSuffix(",")
            val disciplineMatcher = DISCIPLINE_PATTERN.matcher(disciplineString)
            if (!disciplineMatcher.matches()) throw ParsingException("Can't parse discipline string: $disciplineString")

            val discipline = disciplineMatcher.group(1)
            val type = extractType(disciplineMatcher.group(2))

            val subgroup = disciplineMatcher.group(3)?.toInt() ?: 0

            val auditory = data.getElementsByClass("aud").text().trim()

            val teacherElements = data.getElementsByClass("teac")
            val teacher = if (teacherElements.isNotEmpty()) teacherElements.text().trim() else ""

            lessons += Lesson(parity, currentWeekday, time, discipline, auditory, teacher, type, subgroup)
        }

        return lessons
    }

    private fun extractParity(parityText: String) = when (parityText) {
        ODD_PARITY -> Parity.ODD
        EVEN_PARITY -> Parity.EVEN
        else -> throw ParsingException("Unknown parity: $parityText")
    }

    private fun extractType(typeText: String) = when (typeText) {
        PRACTICE -> Lesson.Type.PRACTICE
        LECTURE -> Lesson.Type.LECTURE
        LABORATORY -> Lesson.Type.LABORATORY
        else -> throw ParsingException("Unknown type: $typeText")
    }

}
