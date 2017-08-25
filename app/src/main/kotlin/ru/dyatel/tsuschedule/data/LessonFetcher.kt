package ru.dyatel.tsuschedule.data

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.parsing.BadGroupException
import ru.dyatel.tsuschedule.parsing.Parser
import ru.dyatel.tsuschedule.parsing.ParsingException
import ru.dyatel.tsuschedule.schedulePreferences
import java.io.IOException
import java.net.SocketTimeoutException

fun asyncLessonFetch(context: Context, data: LessonDao) = with(context) {
    val preferences = schedulePreferences

    val group = preferences.group
    if (group.isBlank()) {
        EventBus.broadcast(Event.DATA_UPDATE_FAILED)
        longToast(R.string.failure_missing_group_index)
        return@with
    }

    doAsync {
        val parser = Parser()
        parser.setTimeout(preferences.connectionTimeout * 1000)

        try {
            data.update(parser.getLessons(group))
        } catch (e: Exception) {
            EventBus.broadcast(Event.DATA_UPDATE_FAILED)
            val failureTextRes = when (e) {
                is BadGroupException -> R.string.failure_wrong_group_index
                is ParsingException -> {
                    if (BuildConfig.DEBUG) Log.e("LessonFetcher", "Failed to parse the response", e)
                    else Crashlytics.logException(e)
                    R.string.failure_parsing_failed
                }
                is SocketTimeoutException -> R.string.failure_connection_timeout
                is IOException -> R.string.failure_unsuccessful_request
                else -> {
                    uiThread { throw e }
                    return@doAsync
                }
            }

            uiThread { longToast(failureTextRes) }
        }
    }
}
