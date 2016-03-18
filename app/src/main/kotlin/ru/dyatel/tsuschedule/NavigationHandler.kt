package ru.dyatel.tsuschedule

import android.support.v4.app.FragmentManager
import android.view.MenuItem
import ru.dyatel.tsuschedule.layout.NavigationDrawerHandler
import android.R as AR

class NavigationHandler(
        private val fragmentManager: FragmentManager,
        private val drawerHandler: NavigationDrawerHandler
) : FragmentManager.OnBackStackChangedListener {

    override fun onBackStackChanged() {
        drawerHandler.enabled = fragmentManager.backStackEntryCount == 0
    }

    fun onBackPressed(): Boolean {
        if (drawerHandler.isDrawerOpened()) {
            drawerHandler.closeDrawer()
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
        if (item.itemId == AR.id.home && onBackPressed()) return true
        return false
    }

}
