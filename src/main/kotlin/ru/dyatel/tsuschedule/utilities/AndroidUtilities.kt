package ru.dyatel.tsuschedule.utilities

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.preference.Preference
import android.support.v4.app.NotificationManagerCompat
import android.view.View
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.notificationManager
import org.json.JSONArray
import org.json.JSONObject
import ru.dyatel.tsuschedule.NOTIFICATION_CHANNEL_UPDATES
import ru.dyatel.tsuschedule.R

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

inline fun <reified T> JSONObject.find(name: String): T =
        get(name) as T? ?: throw NoSuchElementException()

operator fun JSONArray.iterator() = object : Iterator<Any> {

    private var current = 0

    override fun hasNext() = current < this@iterator.length()

    override fun next() = this@iterator[current++]

}

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        return

    val notificationManager = context.notificationManager

    val name = context.getString(R.string.notification_channel_updates_name)
    val updateChannel = NotificationChannel(NOTIFICATION_CHANNEL_UPDATES, name, NotificationManagerCompat.IMPORTANCE_LOW)
    notificationManager.createNotificationChannel(updateChannel)
}

val Screen<*>.ctx: Context?
    get() = getActivity()
