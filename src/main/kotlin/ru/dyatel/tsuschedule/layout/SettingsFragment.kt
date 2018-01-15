package ru.dyatel.tsuschedule.layout

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import org.jetbrains.anko.ctx
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.runOnUiThread
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.NOTIFICATION_UPDATE
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.updater.Updater
import ru.dyatel.tsuschedule.utilities.NumberPreferenceValidator
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class SettingsFragment : PreferenceFragment(), EventListener {

    private lateinit var updateButton: Preference
    private var updateAvailable = false

    private lateinit var updater: Updater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        updater = Updater(ctx)

        preferenceScreen.findPreference(getString(R.string.preference_timeout))
                .onPreferenceChangeListener = NumberPreferenceValidator(constraint = 1..30)

        updateButton = preferenceScreen.findPreference(getString(R.string.preference_update)).apply {
            setOnPreferenceClickListener {
                activity.notificationManager.cancel(NOTIFICATION_UPDATE)

                if (updateAvailable)
                    updater.installDialog { longSnackbar(view, it) }
                else
                    updater.checkDialog { longSnackbar(view, it) }

                true
            }
        }
        syncUpdateButton(ctx.schedulePreferences.lastRelease)

        preferenceScreen.findPreference(getString(R.string.preference_version)).summary = BuildConfig.VERSION_NAME

        EventBus.subscribe(this, Event.PREFERENCES_LATEST_VERSION_CHANGED)
    }

    override fun onDestroy() {
        EventBus.unsubscribe(this)
        super.onDestroy()
    }

    override fun handleEvent(type: Event, payload: Any?) = runOnUiThread { syncUpdateButton(payload as String?) }

    private fun syncUpdateButton(lastRelease: String?) {
        updateAvailable = lastRelease != null
        if (updateAvailable)
            updateButton.setTitle(R.string.preference_update_install_title)
        else
            updateButton.setTitle(R.string.preference_update_check_title)
    }

}
