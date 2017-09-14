package ru.dyatel.tsuschedule.screens

import android.content.Context
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus

class FilterScreenView(context: Context) : BaseScreenView<FilterScreen>(context)

class FilterScreen : Screen<FilterScreenView>() {

    override fun createView(context: Context) = FilterScreenView(context)

    override fun onShow(context: Context) {
        super.onShow(context)
        EventBus.broadcast(Event.DISABLE_NAVIGATION_DRAWER)
    }

    override fun onHide(context: Context) {
        EventBus.broadcast(Event.ENABLE_NAVIGATION_DRAWER)
        super.onHide(context)
    }

}
