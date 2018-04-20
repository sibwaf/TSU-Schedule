package ru.dyatel.tsuschedule.parsing

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExamScheduleParserTest {

    private val requester = DataRequester()

    private fun request(group: String) = ExamScheduleParser.parse(requester.examSchedule(group))

    private fun check(group: String) {
        val exams = request(group)

        assertFalse(exams.isEmpty(), "No exams were received!")
    }

    @Test fun testBadGroup() {
        assertTrue(request("221").isEmpty(), "Got exams for non-existing group")
    }

    @Test fun test221251() = check("221251")

    @Test fun test221261() = check("221261")

    @Test fun test221271() = check("221271")

    @Test fun test230751() = check("230751")

    @Test fun test132361() = check("132361")

    @Test fun test420851() = check("420851")

    @Test fun test520761() = check("520761")

    @Test fun test622641() = check("622641")

    @Test fun test930169() = check("930169")

}
