package ru.dyatel.tsuschedule.utilities

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationManagerCompat.IMPORTANCE_LOW
import org.jetbrains.anko.notificationManager
import ru.dyatel.tsuschedule.NOTIFICATION_CHANNEL_UPDATES
import ru.dyatel.tsuschedule.R

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.notificationManager

        val name = context.getString(R.string.notification_channel_updates_name)
        val updateChannel = NotificationChannel(NOTIFICATION_CHANNEL_UPDATES, name, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(updateChannel)
    }
}
