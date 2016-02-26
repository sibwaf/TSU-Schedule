package ru.dyatel.tsuschedule.parsing

data class Lesson(
        val parity: Parity,

        val weekday: String,
        val time: String,

        val discipline: String,
        val auditory: String,
        val teacher: String,

        val type: Type,
        val subgroup: Int
) : Comparable<Lesson> {

    enum class Type {
        PRACTICE, LECTURE, LABORATORY, UNKNOWN
    }

    override fun compareTo(other: Lesson): Int =
            time.compareTo(other.time)

}
