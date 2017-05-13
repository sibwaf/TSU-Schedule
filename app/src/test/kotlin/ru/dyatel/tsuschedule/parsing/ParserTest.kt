package ru.dyatel.tsuschedule.parsing

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.not
import org.junit.Test
import java.util.HashSet

class ParserTest {

    private var parser = Parser()

    init {
        parser.setTimeout(30000)
    }

    private fun check(group: String) {
        val lessons = parser.getLessons(group)
        assertThat("lessons", lessons, not(empty()))

        val weekdays = lessons
                .map { it.weekday }
                .distinct()
                .toCollection(HashSet())

        assertThat("weekday count", weekdays.size, greaterThanOrEqualTo(3))
    }

    @Test(expected = BadGroupException::class)
    fun testNoGroup() = check("")

    @Test(expected = BadGroupException::class)
    fun testBadGroup() = check("221")

    @Test fun test221251() = check("221251")

    @Test fun test230751() = check("230751")

    @Test fun test930169() = check("930169")

    @Test fun test720541() = check("720541-ПБ")

    @Test fun test320842() = check("320842")

    @Test fun test820431() = check("820431")

    @Test fun test132361() = check("132361")

    @Test fun test420851() = check("420851")

    @Test fun test520761() = check("520761")

    @Test fun test622641() = check("622641")

}
