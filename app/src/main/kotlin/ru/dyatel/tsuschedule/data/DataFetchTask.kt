package ru.dyatel.tsuschedule.data

import android.os.AsyncTask
import ru.dyatel.tsuschedule.parsing.Lesson
import ru.dyatel.tsuschedule.parsing.Parser
import java.io.IOException
import java.net.SocketTimeoutException

class DataFetchTask : AsyncTask<String, Void, Set<Lesson>?>() {

    enum class Failure {
        NONE, UNKNOWN, NO_GROUP, WRONG_GROUP, TIMEOUT, CONNECTION_FAIL
    }

    var failure: Failure = Failure.NONE
        private set

    override fun doInBackground(vararg params: String): Set<Lesson>? {
        val group = params[0]
        if (group.isBlank()) {
            failure = Failure.NO_GROUP
            return null
        }

        var lessons: Set<Lesson>? = null
        try {
            lessons = Parser.getLessons(group)
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
