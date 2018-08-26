package ru.dyatel.tsuschedule.utilities

// Sunday..Saturday -> Monday..Sunday
fun convertWeekday(weekday: Int) = if (weekday == 1) 7 else weekday - 1
