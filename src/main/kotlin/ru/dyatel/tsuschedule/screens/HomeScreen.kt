package ru.dyatel.tsuschedule.screens

import android.content.Context
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class HomeView(context: Context) : BaseScreenView<HomeScreen>(context)

class HomeScreen : Screen<HomeView>() {

    override fun createView(context: Context) = HomeView(context)

    override fun onShow(context: Context) {
        super.onShow(context)

        val preferences = context.schedulePreferences
        preferences.group
                .takeIf { it in preferences.groups }
                ?.let { EventBus.broadcast(Event.NAVIGATION_TO, it) }
    }
}
