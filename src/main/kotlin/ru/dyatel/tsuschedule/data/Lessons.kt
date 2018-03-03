package ru.dyatel.tsuschedule.data

open class BaseLesson(
        val parity: Parity,

        val weekday: String,
        val time: String,

        val discipline: String,
        val auditory: String?,

        val type: LessonType
) : Comparable<BaseLesson> {

    override fun compareTo(other: BaseLesson) = time.compareTo(other.time)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as? BaseLesson ?: return false

        if (parity != other.parity) return false
        if (weekday != other.weekday) return false
        if (time != other.time) return false
        if (discipline != other.discipline) return false
        if (auditory != other.auditory) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parity.hashCode()
        result = 31 * result + weekday.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + discipline.hashCode()
        result = 31 * result + (auditory?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }

}

class Lesson(
        parity: Parity,

        weekday: String,
        time: String,

        discipline: String,
        auditory: String?,
        val teacher: String?,

        type: LessonType,
        val subgroup: Int?
) : BaseLesson(parity, weekday, time, discipline, auditory, type) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as? Lesson ?: return false
        if (!super.equals(other)) return false

        if (teacher != other.teacher) return false
        if (subgroup != other.subgroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (teacher?.hashCode() ?: 0)
        result = 31 * result + (subgroup ?: 0)
        return result
    }

}

class TeacherLesson(
        parity: Parity,

        weekday: String,
        time: String,

        discipline: String,
        auditory: String?,

        type: LessonType,

        val groups: List<String>
) : BaseLesson(parity, weekday, time, discipline, auditory, type) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as? TeacherLesson ?: return false
        if (!super.equals(other)) return false

        if (groups != other.groups) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + groups.hashCode()
        return result
    }

}

enum class LessonType {
    PRACTICE, LECTURE, LABORATORY, UNKNOWN
}
