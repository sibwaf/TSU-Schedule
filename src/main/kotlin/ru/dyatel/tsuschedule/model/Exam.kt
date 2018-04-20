package ru.dyatel.tsuschedule.model

import hirondelle.date4j.DateTime

data class Exam(
        val datetime: DateTime,
        val discipline: String,
        val auditory: String,
        val teacher: String
) : Comparable<Exam> {

    override fun compareTo(other: Exam) = datetime.compareTo(other.datetime)

}
