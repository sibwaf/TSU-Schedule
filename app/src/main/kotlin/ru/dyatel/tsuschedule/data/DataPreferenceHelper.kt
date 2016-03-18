package ru.dyatel.tsuschedule.data

import android.content.Context

private const val DATA_PREFERENCES = "data_preferences"
private const val PREFERENCES_GROUP = "group"
private const val PREFERENCES_SUBGROUP = "subgroup"

private fun getPreferences(context: Context) =
        context.getSharedPreferences(DATA_PREFERENCES, Context.MODE_PRIVATE)

fun getGroup(context: Context) =
        getPreferences(context).getString(PREFERENCES_GROUP, "")

fun setGroup(group: String, context: Context) =
        getPreferences(context).edit()
                .putString(PREFERENCES_GROUP, group)
                .apply()

fun getSubgroup(context: Context) =
        getPreferences(context).getInt(PREFERENCES_SUBGROUP, 1)

fun setSubgroup(subgroup: Int, context: Context) =
        getPreferences(context).edit()
                .putInt(PREFERENCES_SUBGROUP, subgroup)
                .apply()