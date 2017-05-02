package ru.dyatel.tsuschedule

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.fragments.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: Drawer

    private lateinit var navigationHandler: NavigationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerHeader = layoutInflater.inflate(R.layout.navigation_drawer, null, false)

        drawer = DrawerBuilder()
                .withActivity(this)
                .withRootView(R.id.drawer_layout)
                .withToolbar(toolbar)
                .withStickyHeader(drawerHeader)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .build()

        navigationHandler = NavigationHandler(fragmentManager, drawer, supportActionBar)

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, MainFragment())
                .commit()
    }

    override fun onBackPressed() {
        if (!navigationHandler.onBackPressed()) super.onBackPressed()
    }

}
