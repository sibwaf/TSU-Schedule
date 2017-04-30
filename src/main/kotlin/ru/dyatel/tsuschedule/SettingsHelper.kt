package ru.dyatel.tsuschedule

import android.content.Context
import android.preference.PreferenceManager

private fun getPreferences(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context)

fun getConnectionTimeout(context: Context): Int {
    val timeoutPreference = context.getString(R.string.preference_timeout)
    val fallback = context.getString(R.string.preference_timeout_default)

    try {
        return getPreferences(context).getString(timeoutPreference, fallback).toInt()
    } catch(e: Exception) {
        return fallback.toInt()
    }
}
