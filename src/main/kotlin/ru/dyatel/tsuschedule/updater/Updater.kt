package ru.dyatel.tsuschedule.updater

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.utilities.download
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.io.File
import java.io.InterruptedIOException
import java.net.URL

class Updater(private val context: Context) {

    private val preferences = context.schedulePreferences

    private val api = UpdaterApi()

    fun fetchUpdateLink(): Release? {
        api.setTimeout(preferences.connectionTimeout * 1000)

        val release = api.getLatestRelease(preferences.allowPrerelease)
                ?.takeIf { Release.CURRENT < it }
        preferences.lastRelease = release?.url
        return release
    }

    fun installUpdate(file: File) {
        val uri = UpdateFileProvider.getUriForFile(context, file)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, uri)
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        context.startActivity(intent)
    }

    fun checkDialog(showMessage: (Int) -> Unit = {}): ProgressDialog =
        context.indeterminateProgressDialog(R.string.update_finding_latest) {
            val task = doAsync {
                try {
                    val release = fetchUpdateLink()
                    uiThread {
                        if (release == null) showMessage(R.string.update_not_found)
                        else showMessage(R.string.update_found)
                    }
                } catch (e: Exception) {
                    if (e !is InterruptedException && e !is InterruptedIOException)
                        uiThread { e.handle { showMessage(it) } }
                } finally {
                    uiThread { dismiss() }
                }
            }

            setOnCancelListener { task.cancel(true) }
        }

    fun installDialog(showMessage: (Int) -> Unit = {}) {
        checkDialog().setOnDismissListener {
            val preferences = context.schedulePreferences
            val link = preferences.lastRelease
            if (link == null) {
                showMessage(R.string.update_not_found)
                return@setOnDismissListener
            }

            context.progressDialog(R.string.update_downloading) {
                setProgressNumberFormat(null)
                max = 100

                val task = doAsync {
                    try {
                        val file = UpdateFileProvider.getUpdateDirectory(context).resolve("update.apk")
                        URL(link).download(file, preferences.connectionTimeout * 1000) { value ->
                            uiThread { progress = value }
                        }
                        installUpdate(file)
                        preferences.lastRelease = null
                    } catch (e: Exception) {
                        if (e !is InterruptedException && e !is InterruptedIOException)
                            uiThread { e.handle { showMessage(it) } }
                    } finally {
                        uiThread { dismiss() }
                    }
                }

                setOnCancelListener { task.cancel(true) }
            }
        }
    }

}
