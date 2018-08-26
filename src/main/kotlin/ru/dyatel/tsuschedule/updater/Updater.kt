package ru.dyatel.tsuschedule.updater

import android.app.Activity
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.wealthfront.magellan.support.SingleActivity
import hirondelle.date4j.DateTime
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.progressDialog
import ru.dyatel.tsuschedule.BadGroupException
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.INTENT_TYPE
import ru.dyatel.tsuschedule.INTENT_TYPE_UPDATE
import ru.dyatel.tsuschedule.MIME_APK
import ru.dyatel.tsuschedule.MainActivity
import ru.dyatel.tsuschedule.NOTIFICATION_CHANNEL_UPDATES
import ru.dyatel.tsuschedule.NOTIFICATION_UPDATE
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.database.database
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.screens.ChangelogScreen
import ru.dyatel.tsuschedule.utilities.Validator
import ru.dyatel.tsuschedule.utilities.download
import ru.dyatel.tsuschedule.utilities.getContentUri
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.io.File
import java.net.URL
import java.util.TimeZone

class Updater(private val activity: Activity) {

    companion object {
        fun getUpdateFile(context: Context) = context.cacheDir.resolve("update.apk")
    }

    private val context: Context = activity
    private val preferences = context.schedulePreferences

    private val database = activity.database

    private val api = UpdaterApi()

    fun fetchUpdate(notify: Boolean): ReleaseToken? {
        val allowPrerelease = preferences.allowPrerelease

        api.setTimeout(preferences.connectionTimeout)

        val releases = api.getReleases()
        database.changelogs.save(releases)

        val update = releases
                .sortedByDescending { it.release }
                .firstOrNull { !it.prerelease || allowPrerelease }
                ?.takeIf { Release.CURRENT < it.release }

        if (notify && update != null && preferences.lastReleaseUrl != update.url) {
            showNotification(update.release)
        }

        preferences.lastUpdateCheck = DateTime.now(TimeZone.getDefault())
        preferences.lastReleaseUrl = update?.url

        return update
    }

    fun installUpdate(file: File) {
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                .setDataAndType(file.getContentUri(context), MIME_APK)
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        activity.startActivityForResult(intent, 0)
    }

    fun handleMigration() {
        val lastUsedVersion = preferences.lastUsedVersion ?: -1
        if (lastUsedVersion == BuildConfig.VERSION_CODE) {
            return
        }

        if (lastUsedVersion < 12) {
            try {
                preferences.group
                        ?.let { Validator.validateGroup(it) }
                        ?.let { preferences.addGroup(it) }
            } catch (e: BadGroupException) {
            }
        }

        if (lastUsedVersion < 15) {
            @Suppress("deprecation")
            val oldSchedule = database.oldSchedule

            preferences.groups.forEach {
                val lessons = oldSchedule.request(it)
                if (lessons.isNotEmpty()) {
                    oldSchedule.remove(it)
                    database.snapshots.save(it, lessons)
                }
            }
        }

        if (lastUsedVersion >= 15) {
            preferences.pendingChangelogDisplay = true
        }

        preferences.lastUsedVersion = BuildConfig.VERSION_CODE
    }

    fun showNotification(release: Release) {
        val intent = context.intentFor<MainActivity>(INTENT_TYPE to INTENT_TYPE_UPDATE)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_UPDATES)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(context.getString(R.string.notification_update_found_title, release.version))
                .setContentText(context.getString(R.string.notification_update_found_description))
                .setContentIntent(pending)
                .build()

        context.notificationManager.notify(NOTIFICATION_UPDATE, notification)
    }

    fun showChangelog() {
        SingleActivity.getNavigator().goTo(ChangelogScreen())
    }

    fun checkDialog(showMessage: (Int) -> Unit = {}): ProgressDialog {
        return context.indeterminateProgressDialog(R.string.update_finding_latest) {
            val task = launch(UI) {
                try {
                    val release = async { fetchUpdate(false) }.await()

                    val message = if (release == null) R.string.update_not_found else R.string.update_found
                    showMessage(message)
                } catch (e: Exception) {
                    e.handle { showMessage(it) }
                } finally {
                    dismiss()
                }
            }

            setOnCancelListener { task.cancel() }
        }
    }

    fun installDialog(showMessage: (Int) -> Unit = {}) {
        checkDialog().setOnDismissListener {
            val link = preferences.lastReleaseUrl ?: run {
                showMessage(R.string.update_not_found)
                return@setOnDismissListener
            }

            context.progressDialog(R.string.update_downloading) {
                setProgressNumberFormat(null)
                max = 100

                val file = getUpdateFile(context)
                val task = launch(UI) {
                    try {
                        async {
                            URL(link).download(file, preferences.connectionTimeout) { value ->
                                launch(UI) { progress = value }
                            }
                        }.await()

                        installUpdate(file)
                        preferences.lastReleaseUrl = null
                    } catch (e: Exception) {
                        e.handle(showMessage)
                    } finally {
                        dismiss()
                    }
                }

                setOnCancelListener { task.cancel() }
            }
        }
    }

}
