package ru.dyatel.tsuschedule.screens

import android.app.FragmentManager
import android.content.Context
import android.view.View
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.layout.SettingsFragment

class PreferenceView(context: Context) : BaseScreenView<PreferenceScreen>(context) {

    private val fragment = SettingsFragment()

    private val container = frameLayout {
        id = View.generateViewId()
        lparams {
            width = matchParent
            height = matchParent
        }
    }

    fun attachFragment(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
                .add(container.id, fragment)
                .commit()
    }

    fun detachFragment(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
                .detach(fragment)
                .commit()
    }

}

class PreferenceScreen : Screen<PreferenceView>() {

    override fun createView(context: Context) = PreferenceView(context)

    override fun onShow(context: Context) {
        super.onShow(context)
        EventBus.broadcast(Event.DISABLE_NAVIGATION_DRAWER)
        view.attachFragment(activity.fragmentManager)
    }

    override fun onHide(context: Context?) {
        view.detachFragment(activity.fragmentManager)
        EventBus.broadcast(Event.ENABLE_NAVIGATION_DRAWER)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_settings)!!

}
