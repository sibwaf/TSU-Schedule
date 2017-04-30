package ru.dyatel.tsuschedule

import android.app.FragmentManager
import android.view.MenuItem
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.layout.MenuEntry
import ru.dyatel.tsuschedule.layout.NavigationDrawerHandler

class NavigationHandler(
        private val fragmentManager: FragmentManager,
        private val drawerHandler: NavigationDrawerHandler
) : FragmentManager.OnBackStackChangedListener, EventListener {

    init {
        EventBus.subscribe(this, Event.NAVIGATE_TO)
    }

    override fun onBackStackChanged() {
        drawerHandler.enabled = fragmentManager.backStackEntryCount == 0
    }

    fun onBackPressed(): Boolean {
        if (drawerHandler.opened) {
            drawerHandler.close()
            return true
        }
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerHandler.onOptionsItemSelected(item)) return true
        if (item.itemId == android.R.id.home && onBackPressed()) return true
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
