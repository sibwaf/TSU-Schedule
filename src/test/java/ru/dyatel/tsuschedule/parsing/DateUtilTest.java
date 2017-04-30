package ru.dyatel.tsuschedule.parsing;

import hirondelle.date4j.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateUtilTest {

    @Test
    public void testOddWeeks() {
        // September
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 1)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 6)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 14)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 17)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 20)));

        // October
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 1)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 4)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 12)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 15)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 18)));

        // January
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 1, 4)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 1, 7)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 1, 10)));

        // September
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 9, 1)));
        assertEquals(Parity.ODD, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 9, 4)));
    }

    @Test
    public void testEvenWeeks() {
        // September
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 7)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 10)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 9, 13)));

        // October
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 5)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 8)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 11)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 19)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 22)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2015, 10, 25)));

        // January
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 1, 1)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 1, 3)));

        // September
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 9, 5)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 9, 7)));
        assertEquals(Parity.EVEN, DateUtil.getWeekParity(DateTime.forDateOnly(2016, 9, 11)));
    }

}
