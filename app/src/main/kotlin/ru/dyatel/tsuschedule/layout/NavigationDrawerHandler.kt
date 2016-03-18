package ru.dyatel.tsuschedule.layout

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.support.v4.app.FragmentManager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import hirondelle.date4j.DateTime
import ru.dyatel.tsuschedule.ParityReference
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.DataFragment
import ru.dyatel.tsuschedule.parsing.DateUtil
import java.util.TimeZone
import android.R as AR

private const val drawerGravity = Gravity.LEFT

class NavigationDrawerHandler(
        activity: Activity,
        fragmentManager: FragmentManager,
        dataFragment: DataFragment,
        private val layout: DrawerLayout
) {

    private val toggle: ActionBarDrawerToggle

    init {
        val groupIndexEdit = layout.findViewById(R.id.group_index) as EditText
        val subgroupSpinner = layout.findViewById(R.id.subgroup) as Spinner

        // Initialize drawer layout
        manageLayout(
                layout.findViewById(R.id.current_parity) as TextView,
                groupIndexEdit, subgroupSpinner, dataFragment,
                activity
        )

        // Initialize app menu
        createAppMenu(
                fragmentManager, layout.context,
                layout.findViewById(R.id.menu_list) as RecyclerView
        )

        // Manage the drawer toggle
        toggle = object : ActionBarDrawerToggle(
                activity, layout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View?) {
                // Save new group and subgroup
                dataFragment.group = groupIndexEdit.text.toString()
                dataFragment.subgroup = subgroupSpinner.selectedItemPosition + 1

                super.onDrawerClosed(drawerView)
            }
        }
        layout.addDrawerListener(toggle)

        // Open the drawer if app is launched for the first time
        openDrawerForTheFirstTime(activity, this)
    }

    fun onConfigurationChanged(config: Configuration) = toggle.onConfigurationChanged(config)
    fun syncState() = toggle.syncState()
    fun onOptionsItemSelected(item: MenuItem): Boolean = toggle.onOptionsItemSelected(item)

    fun isDrawerOpened() = layout.isDrawerOpen(drawerGravity)
    fun openDrawer() = if (enabled) layout.openDrawer(drawerGravity) else Unit
    fun closeDrawer() = layout.closeDrawer(drawerGravity)

    var enabled = true;
        set(enabled) {
            field = enabled;
            layout.setDrawerLockMode(
                    if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
            toggle.isDrawerIndicatorEnabled = enabled
        }

}

private fun manageLayout(
        currentParity: TextView,
        groupIndexEdit: EditText,
        subgroupSpinner: Spinner,
        dataFragment: DataFragment,
        context: Context
) {
    // Show current parity string in the navigation drawer
    currentParity.text = ParityReference.getStringFromParity(DateUtil.getWeekParity(
            DateTime.now(TimeZone.getDefault())
    ))

    // Put current group index into the group index editor
    groupIndexEdit.setText(dataFragment.group)

    // Manage the subgroup spinner
    val spinnerAdapter = ArrayAdapter.createFromResource(
            context, R.array.subgroups, AR.layout.simple_spinner_item
    )
    spinnerAdapter.setDropDownViewResource(AR.layout.simple_spinner_dropdown_item)
    subgroupSpinner.adapter = spinnerAdapter
    subgroupSpinner.setSelection(dataFragment.subgroup - 1)
}

private const val DRAWER_PREFERENCES = "drawer_preferences"
private const val DRAWER_LEARNED_KEY = "drawer_learned"
private fun openDrawerForTheFirstTime(
        context: Context, drawerHandler: NavigationDrawerHandler
) {
    val preferences = context.getSharedPreferences(DRAWER_PREFERENCES, Context.MODE_PRIVATE)

    val alreadySeen = preferences.getBoolean(DRAWER_LEARNED_KEY, false)
    if (!alreadySeen) {
        drawerHandler.openDrawer()
        preferences.edit()
                .putBoolean(DRAWER_LEARNED_KEY, true)
                .apply()
    }
}
