package ru.dyatel.tsuschedule.parsing

import org.junit.Test
import ru.dyatel.tsuschedule.EmptyResultException
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TeacherScheduleParserTest {

    private fun check(teacher: String) {
        val lessons = TeacherScheduleParser(teacher).apply { setTimeout(30000) }.getLessons()
        assertFalse(lessons.isEmpty(), "No lessons were received")

        val weekdayCount = lessons
                .map { it.weekday }
                .distinct()
                .count()

        assertTrue(weekdayCount > 1, "Got too few weekdays")
    }

    @Test fun testBadTeacher() {
        assertFailsWith<EmptyResultException> { check("221") }
    }

    @Test fun test8012() = check("8012")

    @Test fun test56586() = check("56586")

    @Test fun test64548() = check("64548")

    @Test fun test406670() = check("406670")

    @Test fun test912562() = check("912562")

}