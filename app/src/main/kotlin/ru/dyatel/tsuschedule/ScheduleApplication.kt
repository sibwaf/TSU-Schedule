package ru.dyatel.tsuschedule

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import org.acra.ACRA
import org.acra.annotation.ReportsCrashes
import ru.dyatel.tsuschedule.data.DatabaseManager
import ru.dyatel.tsuschedule.events.EventBus

@ReportsCrashes(formUri = BuildConfig.ACRA_BACKEND)
class ScheduleApplication : Application() {

    val eventBus = EventBus()
    val databaseManager = DatabaseManager(this, eventBus)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (!BuildConfig.DISABLE_ACRA) ACRA.init(this)
    }

    override fun onCreate() {
        super.onCreate()

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        ParityReference.init(this)
    }

}
