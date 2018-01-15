package ru.dyatel.tsuschedule

import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.wealthfront.magellan.Navigator
import com.wealthfront.magellan.support.SingleActivity
import com.wealthfront.magellan.transitions.NoAnimationTransition
import hirondelle.date4j.DateTime
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.editText
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.padding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent
import ru.dyatel.tsuschedule.data.currentWeekParity
import ru.dyatel.tsuschedule.data.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.screens.FilterScreen
import ru.dyatel.tsuschedule.screens.HomeScreen
import ru.dyatel.tsuschedule.screens.PreferenceScreen
import ru.dyatel.tsuschedule.screens.ScheduleScreen
import ru.dyatel.tsuschedule.updater.Updater
import ru.dyatel.tsuschedule.utilities.createNotificationChannels
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.util.TimeZone

private const val SCHEDULE_SCREEN_ID_START = 1000

class MainActivity : SingleActivity(), EventListener {

    private lateinit var drawer: Drawer
    private lateinit var parityIndicator: TextView

    private val preferences = schedulePreferences

    private var selectedGroup: String?
        get() {
            val id = drawer.currentSelection - SCHEDULE_SCREEN_ID_START
            return if (id >= 0) preferences.groups[id.toInt()] else null
        }
        set(value) {
            val id = preferences.groups.indexOf(value) + SCHEDULE_SCREEN_ID_START
            drawer.setSelection(id.toLong())

            value?.let { preferences.group = it }
        }

    override fun createNavigator() = Navigator
            .withRoot(HomeScreen())
            .transition(NoAnimationTransition())
            .build()!!

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleUpdateNotification(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        createNotificationChannels(ctx)

        val updater = Updater(ctx)
        updater.handleMigration()

        val toolbar = find<Toolbar>(R.id.toolbar)
        ViewCompat.setElevation(toolbar, resources.getDimension(R.dimen.elevation))
        setSupportActionBar(toolbar)

        val header = ctx.frameLayout {
            parityIndicator = textView {
                gravity = Gravity.CENTER
                textSize = 18f
                text = currentWeekParity.toText(ctx)
            }.lparams {
                width = matchParent
                height = wrapContent
                padding = dip(4)
            }
        }

        drawer = DrawerBuilder()
                .withActivity(this)
                .withRootView(R.id.drawer_layout)
                .withToolbar(toolbar)
                .withStickyHeader(header)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .withOnDrawerListener(drawerListener)
                .withOnDrawerNavigationListener { onBackPressed(); true }
                .withShowDrawerOnFirstLaunch(true)
                .build()

        generateDrawerButtons()

        EventBus.subscribe(this,
                Event.DISABLE_NAVIGATION_DRAWER, Event.ENABLE_NAVIGATION_DRAWER, Event.NAVIGATION_TO)

        if (!handleUpdateNotification(intent)) doAsync { checkUpdates(updater) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.filters).icon = IconicsDrawable(ctx).actionBar()
                .icon(CommunityMaterial.Icon.cmd_filter)
                .colorRes(R.color.text_title_color)
        menu.findItem(R.id.delete_group).icon = IconicsDrawable(ctx).actionBar()
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(R.color.text_title_color)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filters -> getNavigator().goTo(FilterScreen(selectedGroup!!))
            R.id.delete_group -> showDeleteGroupDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun generateDrawerButtons() {
        drawer.removeAllItems()

        drawer.addItem(PrimaryDrawerItem()
                .withIcon(CommunityMaterial.Icon.cmd_plus)
                .withName(R.string.button_add_group)
                .withSelectable(false)
                .withOnDrawerItemClickListener { _, _, _ -> showAddGroupDialog(); true })

        for ((id, group) in preferences.groups.withIndex()) {
            drawer.addItem(PrimaryDrawerItem()
                    .withIdentifier((id + SCHEDULE_SCREEN_ID_START).toLong())
                    .withIcon(CommunityMaterial.Icon.cmd_account_multiple)
                    .withName(group)
                    .withOnDrawerItemClickListener { _, _, _ -> getNavigator().replace(ScheduleScreen(group)); false })
        }

        drawer.addItem(DividerDrawerItem())

        drawer.addItem(PrimaryDrawerItem()
                .withIcon(CommunityMaterial.Icon.cmd_settings)
                .withName(R.string.screen_settings)
                .withOnDrawerItemClickListener { _, _, _ -> getNavigator().goTo(PreferenceScreen()); false }
                .withSelectable(false))
    }

    private fun handleUpdateNotification(intent: Intent): Boolean {
        val result = intent.getStringExtra(INTENT_TYPE) == INTENT_TYPE_UPDATE
        if (result) {
            notificationManager.cancel(NOTIFICATION_UPDATE)
            getNavigator().goTo(PreferenceScreen())
        }
        return result
    }

    private fun checkUpdates(updater: Updater) {
        if (!preferences.autoupdate) return

        val now = DateTime.now(TimeZone.getDefault())
        val shouldCheck = preferences.lastAutoupdate?.plusDays(3)?.lt(now) ?: true
        if (!shouldCheck) return

        try {
            val lastKnown = preferences.lastRelease

            updater.fetchUpdateLink()?.takeIf { it.url != lastKnown }?.run {
                val intent = intentFor<MainActivity>(INTENT_TYPE to INTENT_TYPE_UPDATE)
                val pending = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                val title = getString(R.string.notification_update_found_title, version)

                val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_UPDATES)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(title)
                        .setContentText(getString(R.string.notification_update_found_description))
                        .setContentIntent(pending)
                        .build()

                notificationManager.notify(NOTIFICATION_UPDATE, notification)
            }

            preferences.lastAutoupdate = now
        } catch (e: Exception) {
            e.handle()
        }
    }

    private fun showAddGroupDialog() {
        val view = ctx.frameLayout {
            editText {
                singleLine = true
            }.lparams {
                leftPadding = dip(12)
                rightPadding = dip(12)
                width = matchParent
                height = wrapContent
            }
        }
        val editor = view.getChildAt(0) as EditText

        AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_add_group_title)
                .setMessage(R.string.dialog_add_group_message)
                .setView(view)
                .setPositiveButton(R.string.dialog_ok, { _, _ -> })
                .setNegativeButton(R.string.dialog_cancel, { _, _ -> })
                .show()
                .apply {
                    getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { _ ->
                        val group = editor.text.toString()
                        if (group.isBlank()) {
                            setMessage(getString(R.string.dialog_add_group_message_blank))
                            return@setOnClickListener
                        }
                        if (group in preferences.groups) {
                            setMessage(getString(R.string.dialog_add_group_message_duplicate))
                            return@setOnClickListener
                        }

                        preferences.addGroup(group)

                        generateDrawerButtons()
                        selectedGroup = group
                        drawer.closeDrawer()

                        EventBus.broadcast(Event.INITIAL_DATA_FETCH, group)

                        dismiss()
                    }
                }
    }

    private fun showDeleteGroupDialog() {
        val group = selectedGroup!!

        AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_remove_group_title)
                .setMessage(getString(R.string.dialog_remove_group_message, group))
                .setPositiveButton(R.string.dialog_ok, { _, _ ->
                    val navigator = getNavigator()

                    val groups = preferences.groups
                    val newGroup: String?
                    if (groups.size > 1) {
                        val index = groups.indexOf(group)
                        newGroup = if (index == groups.size - 1) groups[index - 1] else groups[index + 1]
                        selectedGroup = newGroup
                    } else {
                        preferences.group = ""
                        newGroup = null
                        navigator.replace(HomeScreen())
                    }

                    database.lessonDao.remove(group)
                    database.filterDao.removeFilters(group)
                    preferences.removeGroup(group)

                    generateDrawerButtons()
                    newGroup?.let { selectedGroup = it }
                })
                .setNegativeButton(R.string.dialog_cancel, { _, _ -> })
                .show()
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
            Event.NAVIGATION_TO -> selectedGroup = payload as String
        }
    }

    private val drawerListener = object : Drawer.OnDrawerListener {

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

        override fun onDrawerClosed(drawerView: View) = Unit

        override fun onDrawerOpened(drawerView: View) {
            parityIndicator.text = currentWeekParity.toText(ctx)
        }

    }

}
