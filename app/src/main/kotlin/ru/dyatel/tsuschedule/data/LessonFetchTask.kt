package ru.dyatel.tsuschedule.data

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import org.acra.ACRA
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.getConnectionTimeout
import ru.dyatel.tsuschedule.parsing.BadGroupException
import ru.dyatel.tsuschedule.parsing.Parser
import ru.dyatel.tsuschedule.parsing.ParsingException
import java.io.IOException
import java.net.SocketTimeoutException

class LessonFetchTask(
        private val context: Context,
        private val eventBus: EventBus,
        private val data: LessonDao
) : AsyncTask<Void, Void, Void>() {

    private var failureTextRes: Int? = null

    override fun doInBackground(vararg params: Void?): Void? {
        val group = getGroup(context)
        if (group.isNullOrBlank()) {
            failureTextRes = R.string.no_group_index
            return null
        }

        val parser = Parser()
        parser.setTimeout(getConnectionTimeout(context))

        try {
            data.update(parser.getLessons(group))
        } catch (e: BadGroupException) {
            failureTextRes = R.string.wrong_group_index
        } catch (e: ParsingException) {
            failureTextRes = R.string.parsing_failure
        } catch (e: SocketTimeoutException) {
            failureTextRes = R.string.connection_timeout
        } catch (e: IOException) {
            failureTextRes = R.string.load_failure
        } catch (e: Exception) {
            failureTextRes = R.string.unknown_failure
            e.printStackTrace()
            if (!BuildConfig.DISABLE_ACRA) ACRA.getErrorReporter().handleSilentException(e)
        }

        if (failureTextRes != null) eventBus.broadcast(Event.DATA_UPDATE_FAILED)

        return null
    }

    override fun onPostExecute(result: Void?) {
        val error = failureTextRes
        if (error != null) Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }

}