package ru.dyatel.tsuschedule

import android.app.Activity
import ru.dyatel.tsuschedule.data.DatabaseManager
import ru.dyatel.tsuschedule.events.EventBus

private const val error = "Provided activity is not a MainActivity!"

fun Activity.getEventBus(): EventBus =
        if (this is MainActivity) eventBus
        else throw IllegalArgumentException(error)

fun Activity.getDatabaseManager(): DatabaseManager =
        if (this is MainActivity) databaseManager
        else throw IllegalArgumentException(error)