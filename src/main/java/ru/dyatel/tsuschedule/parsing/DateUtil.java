package ru.dyatel.tsuschedule.parsing;

import hirondelle.date4j.DateTime;

/**
 * An utility class to manipulate date.
 */
public class DateUtil {

    /**
     * Finds out if the week of the provided date is
     * even or odd relative to the beginning of the academic year.
     *
     * @param now a date to check
     * @return {@link ru.dyatel.tsuschedule.parsing.Parity#EVEN} if provided date
     * is on a even week, {@link ru.dyatel.tsuschedule.parsing.Parity#ODD} otherwise
     */
    public static Parity getWeekParity(DateTime now) {
        int startYear = now.getYear() - (now.getMonth() < 9 ? 1 : 0);
        DateTime start = DateTime.forDateOnly(startYear, 9, 1);

        int weekDay = (start.getWeekDay() + 6) % 7; // Convert Sunday..Saturday -> Monday..Sunday

        return now.getWeekIndex(start.minusDays(weekDay - 1)) % 2 == 0 ? Parity.EVEN : Parity.ODD;
    }

}
