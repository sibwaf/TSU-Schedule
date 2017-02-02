package ru.dyatel.tsuschedule.layout

import android.support.v4.app.Fragment

class MenuEntry(val name: String, val iconResId: Int, val textResId: Int, val fragmentProvider: () -> Fragment)