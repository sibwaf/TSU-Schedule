package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import ru.dyatel.tsuschedule.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        preferenceScreen.findPreference(getString(R.string.preference_timeout))
                .onPreferenceChangeListener = NumberPreferenceValidator(constraint = 1..30)
    }

}

private class NumberPreferenceValidator(
        val acceptEmptyInput: Boolean = false,
        val constraint: IntRange? = null
) : Preference.OnPreferenceChangeListener {

    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        if (!acceptEmptyInput && newValue == "") return false
        val number: Int
        try {
            if (acceptEmptyInput && newValue == "") number = 0
            else number = (newValue as String).toInt()
        } catch (e: NumberFormatException) {
            return false
        }

        return constraint == null || number in constraint
    }

}
