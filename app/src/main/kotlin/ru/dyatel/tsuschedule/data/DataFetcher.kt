package ru.dyatel.tsuschedule.data

import android.content.Context
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.parsing.Lesson
import ru.dyatel.tsuschedule.parsing.Parser
import java.io.IOException
import java.net.SocketTimeoutException

private val errorStrings = mapOf(
        DataFetcher.Failure.UNKNOWN to R.string.unknown_failure,
        DataFetcher.Failure.NO_GROUP to R.string.no_group_index,
        DataFetcher.Failure.WRONG_GROUP to R.string.wrong_group_index,
        DataFetcher.Failure.TIMEOUT to R.string.connection_timeout,
        DataFetcher.Failure.CONNECTION_FAIL to R.string.load_failure
)

class DataFetcher {

    enum class Failure {
        NONE, UNKNOWN, NO_GROUP, WRONG_GROUP, TIMEOUT, CONNECTION_FAIL
    }

    private var failure: Failure = Failure.NONE
    fun failed() = failure != Failure.NONE

    fun getError(context: Context): String? {
        val stringRes = errorStrings[failure]
        if (stringRes != null) return context.getString(stringRes)
        return ""
    }

    fun fetch(group: String): Set<Lesson>? {
        if (group.isBlank()) {
            failure = Failure.NO_GROUP
            return null
        }

        val parser = Parser();

        var lessons: Set<Lesson>? = null
        try {
            lessons = parser.getLessons(group)
        } catch(e: IllegalArgumentException) {
            failure = Failure.WRONG_GROUP
        } catch(e: SocketTimeoutException) {
            failure = Failure.TIMEOUT
        } catch(e: IOException) {
            failure = Failure.CONNECTION_FAIL
        } catch(e: Exception) {
            failure = Failure.UNKNOWN
        }
        return lessons
    }

}
