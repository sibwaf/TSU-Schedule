package ru.dyatel.tsuschedule

import android.app.FragmentManager
import android.support.v7.app.ActionBar
import com.mikepenz.materialdrawer.Drawer
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.MenuEntry

class NavigationHandler(
        private val fragmentManager: FragmentManager,
        private val drawer: Drawer,
        private val actionBar: ActionBar?
) : FragmentManager.OnBackStackChangedListener, EventListener {

    init {
        EventBus.subscribe(this, Event.NAVIGATE_TO)
    }

    override fun onBackStackChanged() {
        val needDrawer = fragmentManager.backStackEntryCount == 0

        if (needDrawer) actionBar?.setDisplayHomeAsUpEnabled(false)
        drawer.actionBarDrawerToggle.isDrawerIndicatorEnabled = needDrawer
        if (!needDrawer) actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun onBackPressed(): Boolean {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
            return true
        }
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    override fun handleEvent(type: Event, payload: Any?) {
        payload as MenuEntry

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, payload.fragment)
                .addToBackStack(payload.name)
                .commit()
    }

}
