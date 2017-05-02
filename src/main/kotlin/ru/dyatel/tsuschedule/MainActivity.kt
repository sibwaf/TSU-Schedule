package ru.dyatel.tsuschedule

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.fragments.MainFragment
import ru.dyatel.tsuschedule.parsing.currentWeekParity

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: Drawer

    private lateinit var parityIndicator: TextView
    private lateinit var groupEditor: EditText
    private lateinit var subgroupChooser: Spinner

    private lateinit var navigationHandler: NavigationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)

        val preferences = schedulePreferences

        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerHeader = layoutInflater.inflate(R.layout.navigation_drawer, null, false)

        parityIndicator = drawerHeader.find<TextView>(R.id.parity).apply { text = currentWeekParity.toText(ctx) }
        groupEditor = drawerHeader.find<EditText>(R.id.group_index).apply { setText(preferences.group) }
        subgroupChooser = drawerHeader.find<Spinner>(R.id.subgroup).apply {
            adapter = ArrayAdapter
                    .createFromResource(ctx, R.array.subgroups, android.R.layout.simple_spinner_item).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(preferences.subgroup)
        }

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
