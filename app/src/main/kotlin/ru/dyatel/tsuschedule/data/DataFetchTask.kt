package ru.dyatel.tsuschedule.data

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus

class DataFetchTask(
        private val context: Context,
        private val eventBus: EventBus,
        private val data: SavedDataDAO
) : AsyncTask<Void, Void, Void>() {

    private var error: String? = null

    override fun doInBackground(vararg params: Void?): Void? {
        val fetcher = DataFetcher()
        error = fetcher.getError(context)

        val lessons = fetcher.fetch(getGroup(context))
        if (!fetcher.failed()) data.update(lessons)

        return null
    }

    override fun onPostExecute(result: Void?) {
        if (error.isNullOrEmpty()) {
            eventBus.broadcast(Event.DATA_UPDATED)
        } else Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }

}