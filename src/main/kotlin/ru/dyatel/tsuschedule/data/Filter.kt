package ru.dyatel.tsuschedule.data

interface Filter

abstract class ConsumingFilter : Filter{

    abstract fun apply(lesson: Lesson): Lesson?

}
