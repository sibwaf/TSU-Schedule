package ru.dyatel.tsuschedule.layout

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import hirondelle.date4j.DateTime
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.NavigationHandler
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.getGroup
import ru.dyatel.tsuschedule.data.getSubgroup
import ru.dyatel.tsuschedule.data.setGroup
import ru.dyatel.tsuschedule.data.setSubgroup
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.fragments.SettingsFragment
import ru.dyatel.tsuschedule.parsing.DateUtil
import ru.dyatel.tsuschedule.parsing.Parity
import java.util.TimeZone
import android.R as AR

private const val drawerGravity = Gravity.LEFT

class NavigationDrawerHandler(
        private val activity: Activity,
        private val layout: DrawerLayout,
        eventBus: EventBus
) {

    private val toggle: ActionBarDrawerToggle

    var enabled = true
        set(enabled) {
            field = enabled
            layout.setDrawerLockMode(
                    if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
            toggle.isDrawerIndicatorEnabled = enabled
        }

    init {
        manageLayout(layout, activity)

        toggle = object : ActionBarDrawerToggle(
                activity, layout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View?) {
                val groupIndexEdit = layout.find<EditText>(R.id.group_index)
                val subgroupSpinner = layout.find<Spinner>(R.id.subgroup)

                val oldSubgroup = getSubgroup(activity)
                val newSubgroup = subgroupSpinner.selectedItemPosition

                // Save new group and subgroup
                setGroup(groupIndexEdit.text.toString(), activity)
                setSubgroup(newSubgroup, activity)

                // TODO: use DATA_MODIFIER_SET_CHANGED
                if (oldSubgroup != newSubgroup) eventBus.broadcast(Event.DATA_UPDATED)

                super.onDrawerClosed(drawerView)
            }
        }
        layout.addDrawerListener(toggle)

        // Open the drawer if app is launched for the first time
        openDrawerForTheFirstTime(activity, this)
    }

    fun initMenu(navigationHandler: NavigationHandler) {
        val menuAdapter = MenuButtonAdapter(navigationHandler)
        menuAdapter.addEntry(
                MenuEntry("settings", R.drawable.ic_settings, R.string.action_settings, { SettingsFragment() }))

        val menu = layout.findViewById(R.id.menu_list) as RecyclerView
        menu.layoutManager = LinearLayoutManager(activity)
        menu.adapter = menuAdapter
    }

    fun onConfigurationChanged(config: Configuration) = toggle.onConfigurationChanged(config)
    fun syncState() = toggle.syncState()
    fun onOptionsItemSelected(item: MenuItem): Boolean = toggle.onOptionsItemSelected(item)

    fun isDrawerOpened() = layout.isDrawerOpen(drawerGravity)
    fun openDrawer() = if (enabled) layout.openDrawer(drawerGravity) else Unit
    fun closeDrawer() = layout.closeDrawer(drawerGravity)

}

private fun manageLayout(layout: DrawerLayout, context: Context) {
    val highlightView = { enabled: TextView, disabled: TextView ->
        val content = SpannableString(enabled.text)
        content.setSpan(UnderlineSpan(), 0, enabled.text.length, 0)
        enabled.text = content
        enabled.setTextColor(ContextCompat.getColor(context, R.color.enabled_week))

        disabled.text = SpannableString(disabled.text)
        disabled.setTextColor(ContextCompat.getColor(context, R.color.disabled_week))
    }

    val oddWeekText = layout.find<TextView>(R.id.odd_week)
    val evenWeekText = layout.find<TextView>(R.id.even_week)
    if (DateUtil.getWeekParity(DateTime.now(TimeZone.getDefault())) == Parity.ODD) {
        highlightView(oddWeekText, evenWeekText)
    } else {
        highlightView(evenWeekText, oddWeekText)
    }

    layout.find<EditText>(R.id.group_index).setText(getGroup(context))

    val spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.subgroups, AR.layout.simple_spinner_item)
    spinnerAdapter.setDropDownViewResource(AR.layout.simple_spinner_dropdown_item)
    val subgroupSpinner = layout.find<Spinner>(R.id.subgroup)
    subgroupSpinner.adapter = spinnerAdapter
    subgroupSpinner.setSelection(getSubgroup(context))
}

private const val DRAWER_PREFERENCES = "drawer_preferences"
private const val DRAWER_LEARNED_KEY = "drawer_learned"
private fun openDrawerForTheFirstTime(context: Context, drawerHandler: NavigationDrawerHandler) {
    val preferences = context.getSharedPreferences(DRAWER_PREFERENCES, Context.MODE_PRIVATE)

    val alreadySeen = preferences.getBoolean(DRAWER_LEARNED_KEY, false)
    if (!alreadySeen) {
        drawerHandler.openDrawer()
        preferences.edit()
                .putBoolean(DRAWER_LEARNED_KEY, true)
                .apply()
    }
}
