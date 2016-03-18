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

    private var failed = false

    override fun doInBackground(vararg params: Void?): Void? {
        val fetcher = DataFetcher()
        val lessons = fetcher.fetch(getGroup(context))

        if (fetcher.failed()) {
            Toast.makeText(context, fetcher.getError(context), Toast.LENGTH_LONG).show()
            failed = true
            return null
        }

        data.update(lessons)
        return null
    }

    override fun onPostExecute(result: Void?) {
        if (!failed) eventBus.broadcast(Event.DATA_UPDATED)
    }

}