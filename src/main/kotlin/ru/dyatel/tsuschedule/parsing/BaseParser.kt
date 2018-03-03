package ru.dyatel.tsuschedule.parsing

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ru.dyatel.tsuschedule.EmptyResultException
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.data.BaseLesson
import ru.dyatel.tsuschedule.data.LessonType
import ru.dyatel.tsuschedule.data.Parity
import ru.dyatel.tsuschedule.utilities.NullableLateinit
import java.util.HashSet

abstract class BaseParser<out T : BaseLesson> {

    private companion object {
        val WEEKDAY_PATTERN = Regex("\\b[А-Яа-я]+\\b")
    }

    private val connection = Jsoup.connect("http://schedule.tsu.tula.ru/")

    fun setTimeout(timeout: Int) {
        connection.timeout(timeout)
    }

    fun getLessons(): Set<T> {
        val response = prepare(connection).get()
        val result = response.getElementById("results")

        if (result.childNodeSize() <= 1) {
            throw EmptyResultException()
        }

        val lessons = HashSet<T>()
        var currentWeekday: String? = null

        result.children()
                .filter { !it.hasClass("screenonly") }
                .map { it.child(0).child(0).children().last() } // Ignore the padding row
                .forEach {
                    val builder = BaseLessonBuilder()

                    val timeText = it.getElementsByClass("time").requireSingle().text().trim()
                    val weekday = WEEKDAY_PATTERN.find(timeText)?.value ?: currentWeekday
                    currentWeekday = weekday ?: throw ParsingException("Can't find weekday of the lesson")

                    builder.setWeekday(weekday)
                    builder.parseParity(it.getElementsByClass("parity").requireSingle().text())
                    builder.parseTime(timeText)

                    builder.parseDescription(it.getElementsByClass("disc").requireSingle().text())
                    builder.parseAuditory(it.getElementsByClass("aud").requireSingleOrNull()?.text())

                    lessons += parse(it, builder.build())
                }

        return lessons
    }

    protected abstract fun prepare(connection: Connection): Connection

    protected abstract fun parse(e: Element, base: BaseLesson): T

    protected fun <T> Collection<T>.requireSingle() = singleOrNull() ?: throw ParsingException()

    protected fun <T> Collection<T>.requireSingleOrNull() = if (isEmpty()) null else requireSingle()

}

private class BaseLessonBuilder {

    private companion object {
        val BLANK_PARENTHESES_PATTERN = Regex("\\(\\s*\\)")

        val TIME_PATTERN = Regex("\\d{2}:\\d{2}-\\d{2}:\\d{2}")

        val TYPE_MAPPING = mapOf(
                "Пр" to LessonType.PRACTICE,
                "Практ" to LessonType.PRACTICE,
                "Л" to LessonType.LECTURE,
                "Лаб" to LessonType.LABORATORY
        )
        val TYPE_PATTERN = TYPE_MAPPING.keys.joinToString("|", "\\((", ")\\.?\\)").toRegex()
    }

    private lateinit var parity: Parity

    private lateinit var weekday: String
    private lateinit var time: String

    private lateinit var discipline: String
    private var auditory by NullableLateinit<String>()

    private lateinit var type: LessonType

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
        time = TIME_PATTERN.find(text.trim())?.value
                ?: throw ParsingException("Can't parse lesson's time from <$text>")
    }

    fun parseDescription(text: String) {
        text.replace(BLANK_PARENTHESES_PATTERN, "")
                .let {
                    val match = TYPE_PATTERN.find(it)
                    if (match != null) {
                        type = TYPE_MAPPING[match.groupValues[1]]!!
                        it.removeRange(match.range)
                    } else {
                        type = LessonType.UNKNOWN
                        it
                    }
                }
                .trim()
                .removeSuffix(",").trim()
                .let { discipline = it }
    }

    fun parseAuditory(text: String?) {
        auditory = text?.trim()?.takeUnless { it.isEmpty() }
    }

    fun build() = BaseLesson(parity, weekday, time, discipline, auditory, type)

}
