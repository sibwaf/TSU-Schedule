package ru.dyatel.tsuschedule.screens

import android.content.Context
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

class HomeView(context: Context) : BaseScreenView<HomeScreen>(context)

class HomeScreen : Screen<HomeView>() {

    override fun createView(context: Context) = HomeView(context)

}
