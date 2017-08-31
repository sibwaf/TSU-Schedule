package ru.dyatel.tsuschedule.utilities

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import hirondelle.date4j.DateTime
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import java.util.TimeZone

private const val DATA_PREFERENCES = "data_preferences"

private const val PREFERENCES_GROUP = "group"
private const val PREFERENCES_SUBGROUP = "subgroup"

private const val PREFERENCES_LAST_AUTO_UPDATE = "last_auto_update"

private const val PREFERENCES_LAST_RELEASE = "last_release"

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

    val autoupdate: Boolean
        get() {
            val preference = context.getString(R.string.preference_update_auto)
            val fallback = context.getString(R.string.preference_update_auto)
            return preferences.getString(preference, fallback).toBoolean()
        }

    var lastAutoupdate: DateTime?
        get() {
            val timestamp = preferences.getLong(PREFERENCES_LAST_AUTO_UPDATE, -1)

            if (timestamp == -1L) return null
            return DateTime.forInstant(timestamp, TimeZone.getDefault())
        }
        set(value) {
            val timestamp = value?.getMilliseconds(TimeZone.getDefault()) ?: -1
            preferences.editAndApply { putLong(PREFERENCES_LAST_AUTO_UPDATE, timestamp) }
        }

    var lastRelease: String?
        get() = preferences.getString(PREFERENCES_LAST_RELEASE, null)
        set(value) {
            preferences.editAndApply { putString(PREFERENCES_LAST_RELEASE, value) }
            EventBus.broadcast(Event.PREFERENCES_LATEST_VERSION_CHANGED, value)
        }

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