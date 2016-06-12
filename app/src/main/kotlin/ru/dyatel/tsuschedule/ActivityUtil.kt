package ru.dyatel.tsuschedule

import android.app.Activity
import ru.dyatel.tsuschedule.data.LessonDAO
import ru.dyatel.tsuschedule.events.EventBus

private const val error = "Provided activity is not a MainActivity!"

fun Activity.getEventBus(): EventBus =
        if (this is MainActivity) eventBus
        else throw IllegalArgumentException(error)

fun Activity.getLessons(): LessonDAO =
        if (this is MainActivity) lessonDAO
        else throw IllegalArgumentException(error)