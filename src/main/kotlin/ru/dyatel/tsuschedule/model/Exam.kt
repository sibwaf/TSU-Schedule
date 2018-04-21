package ru.dyatel.tsuschedule.model

import hirondelle.date4j.DateTime

data class Exam(
        val discipline: String,
        val consultationDatetime: DateTime?,
        val examDatetime: DateTime,
        val consultationAuditory: String?,
        val examAuditory: String,
        val teacher: String
) : Comparable<Exam> {

    override fun compareTo(other: Exam) = examDatetime.compareTo(other.examDatetime)

}
