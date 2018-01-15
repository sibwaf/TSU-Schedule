package ru.dyatel.tsuschedule.utilities

import android.preference.Preference
import android.view.View

class NumberPreferenceValidator(
        private val acceptEmptyInput: Boolean = false,
        private val constraint: IntRange? = null
) : Preference.OnPreferenceChangeListener {

    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        if (newValue == "")
            return acceptEmptyInput && (constraint == null || 0 in constraint)

        return try {
            val number = (newValue as String).toInt()
            constraint == null || number in constraint
        } catch (e: NumberFormatException) {
            false
        }
    }

}

fun View.hideIf(condition: () -> Boolean) {
    visibility = if (condition()) View.GONE else View.VISIBLE
}
