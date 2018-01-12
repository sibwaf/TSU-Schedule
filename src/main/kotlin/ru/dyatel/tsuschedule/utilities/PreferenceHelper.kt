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

private const val PREFERENCES_LAST_AUTO_UPDATE = "last_auto_update"
private const val PREFERENCES_LAST_RELEASE = "last_release"
private const val PREFERENCES_LAST_USED_VERSION = "last_used_version"

class SchedulePreferences(private val context: Context) {

    private val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)
    private val dataPreferences: SharedPreferences
        get() = context.getSharedPreferences(DATA_PREFERENCES, Context.MODE_PRIVATE)

    var group: String
        get() = dataPreferences.getString(PREFERENCES_GROUP, "")
        set(value) = dataPreferences.editAndApply { putString(PREFERENCES_GROUP, value) }

    val connectionTimeout: Int
        get() {
            val preference = context.getString(R.string.preference_timeout)
            val fallback = context.getString(R.string.preference_timeout_default)
            return preferences.getString(preference, fallback).toInt() // TODO: WTF?
        }

    val autoupdate: Boolean
        get() {
            val preference = context.getString(R.string.preference_update_auto)
            val fallback = context.getString(R.string.preference_update_auto_default).toBoolean()
            return preferences.getBoolean(preference, fallback)
        }

    val allowPrerelease: Boolean
        get() {
            val preference = context.getString(R.string.preference_update_allow_prerelease)
            val fallback = context.getString(R.string.preference_update_allow_prerelease_default).toBoolean()
            return preferences.getBoolean(preference, fallback)
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

    var lastUsedVersion: Int
        get() = preferences.getInt(PREFERENCES_LAST_USED_VERSION, -1)
        set(value) = preferences.editAndApply { putInt(PREFERENCES_LAST_USED_VERSION, value) }

}

val Context.schedulePreferences: SchedulePreferences
    get() = SchedulePreferences(this)

fun SharedPreferences.editAndApply(editor: SharedPreferences.Editor.() -> Unit) {
    val edit = edit()
    edit.editor()
    edit.apply()
}