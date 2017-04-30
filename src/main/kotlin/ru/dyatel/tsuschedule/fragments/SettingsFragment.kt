package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import ru.dyatel.tsuschedule.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

}