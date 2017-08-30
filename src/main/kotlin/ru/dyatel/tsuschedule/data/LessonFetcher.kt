package ru.dyatel.tsuschedule.data

import android.content.Context
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.Parser
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.utilities.schedulePreferences

fun Context.asyncLessonFetch(data: LessonDao) = doAsync {
    val preferences = schedulePreferences

    val parser = Parser()
    parser.setTimeout(preferences.connectionTimeout * 1000)

    try {
        val lessons = parser.getLessons(preferences.group)
        data.update(lessons)
    } catch (e: Exception) {
        EventBus.broadcast(Event.DATA_UPDATE_FAILED)
        uiThread {
            e.handle { longToast(it) }
        }
    }
}
