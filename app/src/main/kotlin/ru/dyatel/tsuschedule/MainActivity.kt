package ru.dyatel.tsuschedule

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
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
        groupEditor = drawerHeader.find<EditText>(R.id.group_index).apply {
            setText(preferences.group)

            setOnEditorActionListener { view, _, _ -> view.clearFocus(); true }
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) return@setOnFocusChangeListener

                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
        subgroupChooser = drawerHeader.find<Spinner>(R.id.subgroup).apply {
            adapter = ArrayAdapter
                    .createFromResource(ctx, R.array.subgroups, android.R.layout.simple_spinner_item).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(preferences.subgroup)
        }

        val settingsButton = PrimaryDrawerItem()
                .withIdentifier(FRAGMENT_SETTINGS)
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withName(R.string.fragment_settings)
                .withSelectable(false)

        drawer = DrawerBuilder()
                .withActivity(this)
                .withRootView(R.id.drawer_layout)
                .withToolbar(toolbar)
                .withStickyHeader(drawerHeader)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(settingsButton)
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener(navigationListener)
                .withOnDrawerListener(drawerListener)
                .withOnDrawerNavigationListener { navigationHandler.onBackPressed() }
                .build()

        navigationHandler = NavigationHandler(fragmentManager, drawer, supportActionBar)
        fragmentManager.addOnBackStackChangedListener(navigationHandler)

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, MainFragment())
                .commit()
    }

    override fun onBackPressed() {
        if (!navigationHandler.onBackPressed()) super.onBackPressed()
    }

    private val navigationListener = Drawer.OnDrawerItemClickListener { _, _, item ->
        EventBus.broadcast(Event.NAVIGATE_TO, item.identifier)
        true
    }

    private val drawerListener = object : Drawer.OnDrawerListener {

        override fun onDrawerSlide(drawerView: View?, slideOffset: Float) = Unit

        override fun onDrawerClosed(drawerView: View?) {
            groupEditor.clearFocus()

            val preferences = ctx.schedulePreferences

            preferences.group = groupEditor.text.toString()

            val oldSubgroup = preferences.subgroup
            val newSubgroup = subgroupChooser.selectedItemPosition
            preferences.subgroup = newSubgroup

            if (oldSubgroup != newSubgroup) EventBus.broadcast(Event.DATA_UPDATED)
        }

        override fun onDrawerOpened(drawerView: View?) {
            parityIndicator.text = currentWeekParity.toText(ctx)
        }

    }

}
