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
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.io.IOException
import java.net.SocketTimeoutException

fun asyncLessonFetch(context: Context, data: LessonDao) = with(context) {
    val preferences = schedulePreferences

    val group = preferences.group
    if (group.isBlank()) {
        fail(R.string.failure_missing_group_index)
        return@with
    }

    doAsync {
        val parser = Parser()
        parser.setTimeout(preferences.connectionTimeout * 1000)

        val failure = try {
            data.update(parser.getLessons(group))
            null
        } catch (e: BadGroupException) {
            R.string.failure_wrong_group_index
        } catch (e: ParsingException) {
            if (BuildConfig.DEBUG) Log.e("LessonFetcher", "Failed to parse the response", e)
            else Crashlytics.logException(e)
            R.string.failure_parsing_failed
        } catch (e: SocketTimeoutException) {
            R.string.failure_connection_timeout
        } catch (e: IOException) {
            R.string.failure_unsuccessful_request
        } catch (e: Exception) {
            uiThread { throw e }
            return@doAsync
        }

        failure?.let { uiThread { fail(failure) } }
    }
}

private fun Context.fail(textResource: Int) {
    EventBus.broadcast(Event.DATA_UPDATE_FAILED)
    longToast(textResource)
}
