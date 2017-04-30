package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.preference.PreferenceFragment
import ru.dyatel.tsuschedule.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

}