package ru.dyatel.tsuschedule.parsing

data class Lesson(
        val parity: Parity,

        val weekday: String,
        val time: String,

        val discipline: String,
        val auditory: String?,
        val teacher: String?,

        val type: LessonType,
        val subgroup: Int?
) : Comparable<Lesson> {

    override fun compareTo(other: Lesson) = time.compareTo(other.time)

}

enum class LessonType {
    PRACTICE, LECTURE, LABORATORY
}
