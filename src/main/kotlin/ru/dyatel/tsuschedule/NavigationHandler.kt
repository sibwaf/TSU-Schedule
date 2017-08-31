package ru.dyatel.tsuschedule

import android.app.Fragment
import android.app.FragmentManager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import com.mikepenz.materialdrawer.Drawer
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.fragments.MainFragment
import ru.dyatel.tsuschedule.fragments.SettingsFragment

private typealias FragmentIdentifier = Long
private typealias FragmentProvider = () -> Fragment

const val FRAGMENT_MAIN: FragmentIdentifier = 0L
const val FRAGMENT_SETTINGS: FragmentIdentifier = 1L

private val FRAGMENTS = mapOf<Long, FragmentProvider>(
        FRAGMENT_MAIN to { MainFragment() },
        FRAGMENT_SETTINGS to { SettingsFragment() }
)

class NavigationHandler(
        private val fragmentManager: FragmentManager,
        private val drawer: Drawer,
        private val actionBar: ActionBar?
) : FragmentManager.OnBackStackChangedListener, EventListener {

    init {
        EventBus.subscribe(this, Event.NAVIGATE_TO)
    }

    override fun onBackStackChanged() {
        val needDrawer = fragmentManager.backStackEntryCount == 1

        if (needDrawer) actionBar?.setDisplayHomeAsUpEnabled(false)
        drawer.actionBarDrawerToggle.isDrawerIndicatorEnabled = needDrawer
        if (!needDrawer) actionBar?.setDisplayHomeAsUpEnabled(true)

        val lockMode = if (needDrawer) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawer.drawerLayout.setDrawerLockMode(lockMode)
    }

    fun onBackPressed(): Boolean {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
            return true
        }
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    override fun handleEvent(type: Event, payload: Any?) {
        payload ?: throw IllegalArgumentException("Can't navigate to fragment with null identifier")
        if (payload !is FragmentIdentifier) throw IllegalArgumentException("Wrong fragment identifier type")

        val fragmentProvider = FRAGMENTS[payload] ?:
                throw IllegalArgumentException("There are no fragments with identifier $payload")

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, fragmentProvider())
                .addToBackStack("navigation_to:fragment_$payload")
                .commit()
    }

}
