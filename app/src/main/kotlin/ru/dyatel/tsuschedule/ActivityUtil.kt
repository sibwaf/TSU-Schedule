package ru.dyatel.tsuschedule

import android.app.Activity
import ru.dyatel.tsuschedule.data.SavedDataDAO
import ru.dyatel.tsuschedule.events.EventBus

private const val error = "Provided activity is not a MainActivity!"

fun Activity.getEventBus(): EventBus =
        if (this is MainActivity) eventBus
        else throw IllegalArgumentException(error)

fun Activity.getData(): SavedDataDAO =
        if (this is MainActivity) data
        else throw IllegalArgumentException(error)