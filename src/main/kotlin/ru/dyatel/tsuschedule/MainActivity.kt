package ru.dyatel.tsuschedule

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.NotificationCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.wealthfront.magellan.Navigator
import com.wealthfront.magellan.support.SingleActivity
import com.wealthfront.magellan.transitions.NoAnimationTransition
import hirondelle.date4j.DateTime
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import ru.dyatel.tsuschedule.data.currentWeekParity
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.screens.PreferenceScreen
import ru.dyatel.tsuschedule.screens.ScheduleScreen
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.util.TimeZone

class MainActivity : SingleActivity(), EventListener {

    private lateinit var drawer: Drawer

    private lateinit var parityIndicator: TextView
    private lateinit var groupEditor: EditText
    private lateinit var subgroupChooser: Spinner

    override fun createNavigator() = Navigator.withRoot(ScheduleScreen())
            .transition(NoAnimationTransition())
            .build()!!

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleUpdateNotification(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        val preferences = schedulePreferences

        val toolbar = find<Toolbar>(R.id.toolbar)
        ViewCompat.setElevation(toolbar, resources.getDimension(R.dimen.elevation))
        setSupportActionBar(toolbar)

        val drawerHeader = layoutInflater.inflate(R.layout.navigation_drawer, null, false)

        parityIndicator = drawerHeader.find<TextView>(R.id.parity).apply {
            ViewCompat.setElevation(this, resources.getDimension(R.dimen.elevation))
            text = currentWeekParity.toText(ctx)
        }
        groupEditor = drawerHeader.find<EditText>(R.id.group_index).apply {
            setText(preferences.group)

            setOnEditorActionListener { view, _, _ -> view.clearFocus(); true }
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) return@setOnFocusChangeListener

                val imm = view.context.inputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
        subgroupChooser = drawerHeader.find<Spinner>(R.id.subgroup).apply {
            adapter = ArrayAdapter
                    .createFromResource(ctx, R.array.subgroups, android.R.layout.simple_spinner_item).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(preferences.subgroup)
        }

        val settingsButton = PrimaryDrawerItem()
                .withIdentifier(NAVIGATION_PREFERENCES)
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withName(R.string.fragment_settings)
                .withSelectable(false)

        drawer = DrawerBuilder()
                .withActivity(this)
                .withRootView(R.id.drawer_layout)
                .withToolbar(toolbar)
                .withStickyHeader(drawerHeader)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(settingsButton)
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener { _, _, item -> navigateTo(item.identifier); true }
                .withOnDrawerListener(drawerListener)
                .withOnDrawerNavigationListener { onBackPressed(); true }
                .withShowDrawerOnFirstLaunch(true)
                .build()

        EventBus.subscribe(this, Event.DISABLE_NAVIGATION_DRAWER, Event.ENABLE_NAVIGATION_DRAWER)

        if (!handleUpdateNotification(intent)) doAsync { checkUpdates() }
    }

    private fun handleUpdateNotification(intent: Intent): Boolean {
        val result = intent.getStringExtra(INTENT_TYPE) == INTENT_TYPE_UPDATE
        if (result) {
            notificationManager.cancel(NOTIFICATION_UPDATE)
            navigateTo(NAVIGATION_PREFERENCES)
        }
        return result
    }

    private fun checkUpdates() {
        val preferences = ctx.schedulePreferences

        if (!preferences.autoupdate) return

        val now = DateTime.now(TimeZone.getDefault())
        val shouldCheck = preferences.lastAutoupdate?.plusDays(3)?.lt(now) ?: true
        if (!shouldCheck) return

        try {
            val release = Updater().apply { setTimeout(preferences.connectionTimeout * 1000) }.getLatestRelease()

            val old = preferences.lastRelease
            val new = release?.takeIf { it.isNewerThanInstalled() }?.url

            preferences.lastRelease = new
            preferences.lastAutoupdate = now

            if (new != null && old != new) {
                val intent = intentFor<MainActivity>(INTENT_TYPE to INTENT_TYPE_UPDATE)
                val pending = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                val title = getString(R.string.notification_update_found_title, release.version)

                val notification = NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(title)
                        .setContentText(getString(R.string.notification_update_found_description))
                        .setContentIntent(pending)
                        .build()

                notificationManager.notify(NOTIFICATION_UPDATE, notification)
            }
        } catch (e: Exception) {
        }
    }

    private fun navigateTo(id: Long) {
        drawer.closeDrawer()

        if (id == NAVIGATION_PREFERENCES) {
            getNavigator().goTo(PreferenceScreen())
            return
        }
    }

    override fun handleEvent(type: Event, payload: Any?) {
        val toggle = drawer.actionBarDrawerToggle
        val layout = drawer.drawerLayout
        val actionBar = supportActionBar

        when (type) {
            Event.DISABLE_NAVIGATION_DRAWER -> {
                toggle.isDrawerIndicatorEnabled = false
                actionBar?.setDisplayHomeAsUpEnabled(true)
                layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            Event.ENABLE_NAVIGATION_DRAWER -> {
                actionBar?.setDisplayHomeAsUpEnabled(false)
                toggle.isDrawerIndicatorEnabled = true
                layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private val drawerListener = object : Drawer.OnDrawerListener {

        override fun onDrawerSlide(drawerView: View?, slideOffset: Float) = Unit

        override fun onDrawerClosed(drawerView: View?) {
            groupEditor.clearFocus()

            val preferences = ctx.schedulePreferences

            preferences.group = groupEditor.text.toString()

            val oldSubgroup = preferences.subgroup
            val newSubgroup = subgroupChooser.selectedItemPosition
            preferences.subgroup = newSubgroup

            if (oldSubgroup != newSubgroup) EventBus.broadcast(Event.DATA_UPDATED)
        }

        override fun onDrawerOpened(drawerView: View?) {
            parityIndicator.text = currentWeekParity.toText(ctx)
        }

    }

}
