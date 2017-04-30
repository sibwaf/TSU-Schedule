package ru.dyatel.tsuschedule.layout

import android.support.v4.app.Fragment
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.fragments.SettingsFragment

class MenuEntry(val name: String, val iconResId: Int, val textResId: Int, private val fragmentProvider: () -> Fragment) {

    val fragment
        get() = fragmentProvider()

}

val APPLICATION_MENU = listOf(
        MenuEntry("settings", R.drawable.ic_settings, R.string.action_settings, { SettingsFragment() })
)