package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import com.wealthfront.magellan.support.SingleActivity
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
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
import ru.dyatel.tsuschedule.utilities.ctx
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class SettingsFragment : PreferenceFragmentCompat(), EventListener {

    private val updater by lazy { Updater(activity!!) }

    private lateinit var updateButton: Preference
    private var updateAvailable = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager.findPreference(getString(R.string.preference_timeout))
                .onPreferenceChangeListener = NumberPreferenceValidator(constraint = 1..30)

        preferenceManager.findPreference(getString(R.string.preference_history_size))
                .onPreferenceChangeListener = NumberPreferenceValidator(constraint = 1..Int.MAX_VALUE)

        updateButton = preferenceManager.findPreference(getString(R.string.preference_update)).apply {
            setOnPreferenceClickListener {
                val activity = activity!!
                activity.notificationManager.cancel(NOTIFICATION_UPDATE)

                val view = view!!
                if (updateAvailable) {
                    updater.installDialog { longSnackbar(view, it) }
                } else {
                    updater.checkDialog { longSnackbar(view, it) }
                }

                true
            }
        }
        syncUpdateButton(ctx!!.schedulePreferences.lastRelease)

        preferenceManager.findPreference(getString(R.string.preference_changelog))
                .setOnPreferenceClickListener { updater.showChangelog(); true }

        preferenceManager.findPreference(getString(R.string.preference_version))
                .summary = BuildConfig.VERSION_NAME

        EventBus.subscribe(this, Event.PREFERENCES_LATEST_VERSION_CHANGED)
    }

    override fun onDestroy() {
        EventBus.unsubscribe(this)
        super.onDestroy()
    }

    override fun handleEvent(type: Event, payload: Any?) {
        ctx!!.runOnUiThread { syncUpdateButton(payload as String?) }
    }

    private fun syncUpdateButton(lastRelease: String?) {
        updateAvailable = lastRelease != null
        if (updateAvailable) {
            updateButton.setTitle(R.string.preference_update_install_title)
        } else {
            updateButton.setTitle(R.string.preference_update_check_title)
        }
    }

}

class PreferenceView(context: Context) : BaseScreenView<PreferenceScreen>(context) {

    private val fragment = SettingsFragment()

    private val container = frameLayout {
        id = View.generateViewId()
        lparams {
            width = matchParent
            height = matchParent
        }
    }

    fun attachFragment(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
                .add(container.id, fragment)
                .commitNow()
    }

    fun detachFragment(fragmentManager: FragmentManager) {
        if (!fragmentManager.isDestroyed) {
            fragmentManager.beginTransaction()
                    .detach(fragment)
                    .commitNow()
        }
    }

}

class PreferenceScreen : Screen<PreferenceView>() {

    private val activity: SingleActivity?
        get() = getActivity() as SingleActivity?

    override fun createView(context: Context) = PreferenceView(context)

    override fun onShow(context: Context) {
        super.onShow(context)
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, false)
        view.attachFragment(activity!!.supportFragmentManager)
    }

    override fun onHide(context: Context?) {
        view.detachFragment(activity!!.supportFragmentManager)
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, true)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_settings)!!

}
