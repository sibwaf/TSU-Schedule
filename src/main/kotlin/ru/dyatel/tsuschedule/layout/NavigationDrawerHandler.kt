package ru.dyatel.tsuschedule.layout

import android.app.Activity
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
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.parsing.Parity
import ru.dyatel.tsuschedule.parsing.currentWeekParity
import ru.dyatel.tsuschedule.schedulePreferences

private const val DRAWER_GRAVITY = Gravity.LEFT

class NavigationDrawerHandler(private val activity: Activity, private val layout: DrawerLayout) {

    private val preferences = activity.schedulePreferences

    private val toggle: ActionBarDrawerToggle

    private val groupIndexEdit = layout.find<EditText>(R.id.group_index)
    private val subgroupSpinner = layout.find<Spinner>(R.id.subgroup)

    var enabled = true
        set(enabled) {
            field = enabled
            layout.setDrawerLockMode(
                    if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
            toggle.isDrawerIndicatorEnabled = enabled
        }

    val opened get() = layout.isDrawerOpen(DRAWER_GRAVITY)

    init {
        val highlightView = { enabled: TextView, disabled: TextView ->
            val content = SpannableString(enabled.text)
            content.setSpan(UnderlineSpan(), 0, enabled.text.length, 0)
            enabled.text = content
            enabled.setTextColor(ContextCompat.getColor(activity, R.color.enabled_week))

            disabled.text = SpannableString(disabled.text)
            disabled.setTextColor(ContextCompat.getColor(activity, R.color.disabled_week))
        }

        val oddWeekText = layout.find<TextView>(R.id.odd_week)
        val evenWeekText = layout.find<TextView>(R.id.even_week)
        if (currentWeekParity == Parity.ODD) {
            highlightView(oddWeekText, evenWeekText)
        } else {
            highlightView(evenWeekText, oddWeekText)
        }

        groupIndexEdit.setText(preferences.group)

        val spinnerAdapter = ArrayAdapter.createFromResource(activity, R.array.subgroups,
                android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val subgroupSpinner = layout.find<Spinner>(R.id.subgroup)
        subgroupSpinner.adapter = spinnerAdapter
        subgroupSpinner.setSelection(preferences.subgroup)

        val menu = layout.find<RecyclerView>(R.id.menu_list)
        menu.layoutManager = LinearLayoutManager(activity)
        menu.adapter = MenuButtonAdapter()

        toggle = object : ActionBarDrawerToggle(activity, layout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerClosed(drawerView: View?) {
                onDrawerClosed()
                super.onDrawerClosed(drawerView)
            }
        }

        layout.addDrawerListener(toggle)

        // Open the drawer if app is launched for the first time
        if (!preferences.drawerIsLearned) {
            open()
            preferences.drawerIsLearned = true
        }
    }

    private fun onDrawerClosed() {
        val oldSubgroup = preferences.subgroup
        val newSubgroup = subgroupSpinner.selectedItemPosition

        // Save new group and subgroup
        preferences.group = groupIndexEdit.text.toString()
        preferences.subgroup = newSubgroup

        // TODO: use DATA_MODIFIER_SET_CHANGED
        if (oldSubgroup != newSubgroup) EventBus.broadcast(Event.DATA_UPDATED)
    }

    fun onConfigurationChanged(config: Configuration) = toggle.onConfigurationChanged(config)
    fun syncState() = toggle.syncState()
    fun onOptionsItemSelected(item: MenuItem): Boolean = toggle.onOptionsItemSelected(item)

    fun open() = if (enabled) layout.openDrawer(DRAWER_GRAVITY) else Unit
    fun close() = layout.closeDrawer(DRAWER_GRAVITY)

}
