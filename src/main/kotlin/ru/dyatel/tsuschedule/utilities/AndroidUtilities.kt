package ru.dyatel.tsuschedule.utilities

import android.app.AlertDialog
import android.preference.Preference

fun AlertDialog.setMessage(id: Int) = setMessage(context.getString(id))

class NumberPreferenceValidator(
        private val acceptEmptyInput: Boolean = false,
        private val constraint: IntRange? = null
) : Preference.OnPreferenceChangeListener {

    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        if (newValue == "") {
            return acceptEmptyInput && (constraint == null || 0 in constraint)
        }

        return try {
            val number = (newValue as String).toInt()
            constraint == null || number in constraint
        } catch (e: NumberFormatException) {
            false
        }
    }

}
