package ru.dyatel.tsuschedule.parsing

import hirondelle.date4j.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.dyatel.tsuschedule.model.Parity
import ru.dyatel.tsuschedule.model.weekParityOf

class WeekParityTest {

    @Test
    fun testAcademicYearStartOnSunday() {
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2013, 9, 1)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2013, 9, 2)))
    }

    @Test
    fun testAcademicYearStartOnMonday() {
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2014, 9, 1)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2014, 9, 8)))
    }

    @Test
    fun testOddSeptember() {
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 6)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 16)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 28)))
    }

    @Test
    fun testOddSameYear() {
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 10, 1)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 10, 12)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 10, 31)))
    }

    @Test
    fun testOddNextYear() {
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2016, 1, 4)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2016, 1, 10)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2016, 1, 21)))
    }

    @Test
    fun testEvenSeptember() {
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 7)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 13)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 24)))
    }

    @Test
    fun testEvenSameYear() {
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 11, 8)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 11, 19)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 11, 30)))
    }

    @Test
    fun testEvenNextYear() {
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2016, 2, 8)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2016, 2, 14)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2016, 2, 25)))
    }

    @Test
    fun testFullOddWeek() {
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 14)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 15)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 16)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 17)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 18)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 19)))
        assertEquals(Parity.ODD, weekParityOf(DateTime.forDateOnly(2015, 9, 20)))
    }

    @Test
    fun testFullEvenWeek() {
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 21)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 22)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 23)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 24)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 25)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 26)))
        assertEquals(Parity.EVEN, weekParityOf(DateTime.forDateOnly(2015, 9, 27)))
    }

}