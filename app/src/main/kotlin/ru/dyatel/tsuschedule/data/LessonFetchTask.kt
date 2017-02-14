package ru.dyatel.tsuschedule.data

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus

class LessonFetchTask(
        private val context: Context,
        private val eventBus: EventBus,
        private val data: LessonDao
) : AsyncTask<Void, Void, Void>() {

    private var error: String? = null

    override fun doInBackground(vararg params: Void?): Void? {
        val fetcher = LessonFetcher(context)

        val lessons = fetcher.fetch(getGroup(context))
        error = fetcher.getError()
        if (!fetcher.failed()) data.update(lessons)

        if (!error.isNullOrEmpty()) eventBus.broadcast(Event.DATA_UPDATE_FAILED)

        return null
    }

    override fun onPostExecute(result: Void?) {
        if (!error.isNullOrEmpty()) Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }

}