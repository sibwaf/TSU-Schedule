package ru.dyatel.tsuschedule

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.fragments.MainFragment
import ru.dyatel.tsuschedule.layout.NavigationDrawerHandler

class MainActivity : AppCompatActivity() {

    private lateinit var drawerHandler: NavigationDrawerHandler
    private lateinit var navigationHandler: NavigationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        val fragmentManager = supportFragmentManager

        setSupportActionBar(find<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val eventBus = (application as ScheduleApplication).eventBus
        val drawer = find<DrawerLayout>(R.id.drawer_layout)
        drawerHandler = NavigationDrawerHandler(this, drawer, eventBus)

        navigationHandler = NavigationHandler(fragmentManager, drawerHandler)
        fragmentManager.addOnBackStackChangedListener(navigationHandler)

        drawerHandler.initMenu(navigationHandler)

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, MainFragment())
                .commit()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerHandler.syncState()
    }

    override fun onBackPressed() {
        if (!navigationHandler.onBackPressed()) super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return navigationHandler.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerHandler.onConfigurationChanged(newConfig)
    }

}
