package ru.dyatel.tsuschedule

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

private const val DATA_PREFERENCES = "data_preferences"

private const val PREFERENCES_GROUP = "group"
private const val PREFERENCES_SUBGROUP = "subgroup"

private const val PREFERENCES_LAST_RELEASE = "last_release"

private const val PREFERENCES_DRAWER_LEARNED = "drawer_learned"

class SchedulePreferences(private val context: Context) {

    var group: String
        get() = dataPreferences.getString(PREFERENCES_GROUP, "")
        set(value) = dataPreferences.editAndApply { putString(PREFERENCES_GROUP, value) }

    var subgroup: Int
        get() = dataPreferences.getInt(PREFERENCES_SUBGROUP, 0)
        set(value) = dataPreferences.editAndApply { putInt(PREFERENCES_SUBGROUP, value) }

    val connectionTimeout: Int
        get() {
            val preference = context.getString(R.string.preference_timeout)
            val fallback = context.getString(R.string.preference_timeout_default)
            return preferences.getString(preference, fallback).toInt()
        }

    val updateAutoCheck: Boolean
        get() {
            val preference = context.getString(R.string.preference_update_check_auto)
            val fallback = context.getString(R.string.preference_update_check_auto)
            return preferences.getString(preference, fallback).toBoolean()
        }

    var lastRelease: String?
        get() = preferences.getString(PREFERENCES_LAST_RELEASE, null)
        set(value) = preferences.editAndApply { putString(PREFERENCES_LAST_RELEASE, value) }

    private val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)
    private val dataPreferences: SharedPreferences
        get() = context.getSharedPreferences(DATA_PREFERENCES, Context.MODE_PRIVATE)

}

val Context.schedulePreferences: SchedulePreferences
    get() = SchedulePreferences(this)

fun SharedPreferences.editAndApply(editor: SharedPreferences.Editor.() -> Unit) {
    val edit = edit()
    edit.editor()
    edit.apply()
}