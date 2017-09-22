package ru.dyatel.tsuschedule.parsing

import org.junit.Test
import ru.dyatel.tsuschedule.BadGroupException
import ru.dyatel.tsuschedule.Parser
import java.util.HashSet
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParserTest {

    private var parser = Parser()

    init {
        parser.setTimeout(30000)
    }

    private fun check(group: String) {
        val lessons = parser.getLessons(group)
        assertFalse(lessons.isEmpty(), "No lessons were received")

        val weekdays = lessons
                .map { it.weekday }
                .distinct()
                .toCollection(HashSet())

        assertTrue(weekdays.size >= 3, "Got too few weekdays")
    }

    @Test fun testNoGroup() {
        assertFailsWith<BadGroupException> { check("") }
    }

    @Test fun testBadGroup() {
        assertFailsWith<BadGroupException> { check("221") }
    }

    @Test fun test221251() = check("221251")

    @Test fun test230751() = check("230751")

    @Test fun test720541() = check("720541-ПБ")

    @Test fun test132361() = check("132361")

    @Test fun test420851() = check("420851")

    @Test fun test520761() = check("520761")

    @Test fun test622641() = check("622641")

}
