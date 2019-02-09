package ru.dyatel.tsuschedule.parsing

import org.junit.Test
import ru.dyatel.tsuschedule.EmptyResultException
import ru.dyatel.tsuschedule.model.LessonType
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupScheduleParserTest {

    private val requester = DataRequester()

    private fun check(group: String) {
        val lessons = GroupScheduleParser.parse(requester.groupSchedule(group))
        assertFalse(lessons.isEmpty(), "No lessons were received")

        val weekdayCount = lessons
                .map { it.weekday }
                .distinct()
                .count()

        assertFalse(lessons.all { it.type == LessonType.UNKNOWN }, "Failed to recognize all lesson types")

        assertTrue(weekdayCount > 1, "Got too few weekdays")
    }

    @Test fun testBadGroup() {
        assertFailsWith<EmptyResultException> { check("221") }
    }

    @Test fun test221251() = check("221251")

    @Test fun test221261() = check("221261")

    @Test fun test221271() = check("221271")

    @Test fun test230751() = check("230751")

    @Test fun test720551() = check("720551-ПБ")

    @Test fun test721075() = check("721075")

    @Test fun test132361() = check("132361")

    @Test fun test420851() = check("420851")

    @Test fun test520761() = check("520761")

    @Test fun test622651() = check("622651")

    @Test fun test930169() = check("930169")

}
