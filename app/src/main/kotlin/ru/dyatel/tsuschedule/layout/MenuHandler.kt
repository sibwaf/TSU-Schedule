package ru.dyatel.tsuschedule.layout

import android.app.FragmentManager
import android.content.Context
import android.support.v7.widget.RecyclerView
import org.solovyev.android.views.llm.LinearLayoutManager
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.fragments.SettingsFragment

fun createAppMenu(fragmentManager: FragmentManager, context: Context, view: RecyclerView) {
    // Create menu buttons
    val settingsButton = MenuButtonAdapter.MenuEntry(R.drawable.ic_settings, R.string.action_settings, {
        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, SettingsFragment())
                .addToBackStack("settings_opened")
                .commit()
    })

    // Manage the app menu
    val menuAdapter = MenuButtonAdapter();
    menuAdapter.addMenuEntry(settingsButton);
    view.layoutManager = LinearLayoutManager(context)
    view.adapter = menuAdapter
}
