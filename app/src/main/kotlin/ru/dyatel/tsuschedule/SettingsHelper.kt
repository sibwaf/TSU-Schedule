package ru.dyatel.tsuschedule

import android.content.Context
import android.preference.PreferenceManager

private fun getPreferences(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context)

fun getConnectionTimeout(context: Context): Int {
    val fallback = context.getString(R.string.default_connection_timeout)
    try {
        return Integer.parseInt(
                getPreferences(context)
                        .getString(
                                context.getString(R.string.preference_timeout),
                                fallback
                        )
        )
    } catch(e: Exception) {
        return Integer.parseInt(fallback)
    }
}
