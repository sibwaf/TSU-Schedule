package ru.dyatel.tsuschedule.updater

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.progressDialog
import ru.dyatel.tsuschedule.BadGroupException
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.utilities.Validator
import ru.dyatel.tsuschedule.utilities.download
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.io.File
import java.net.URL

class Updater(private val context: Context) {

    private val preferences = context.schedulePreferences

    private val api = UpdaterApi()

    fun fetchUpdateLink(): Release? {
        api.setTimeout(preferences.connectionTimeout)

        val release = api.getLatestRelease(preferences.allowPrerelease)?.takeIf { Release.CURRENT < it }
        preferences.lastRelease = release?.url
        return release
    }

    fun installUpdate(file: File) {
        val uri = UpdateFileProvider.getUriForFile(context, file)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, uri)
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        context.startActivity(intent)
    }

    fun handleMigration() {
        if (preferences.lastUsedVersion <= 11) {
            try {
                preferences.group
                        ?.let { Validator.validateGroup(it) }
                        ?.let { preferences.addGroup(it) }
            } catch (e: BadGroupException) {
            }
        }
        preferences.lastUsedVersion = BuildConfig.VERSION_CODE
    }

    fun checkDialog(showMessage: (Int) -> Unit = {}): ProgressDialog {
        return context.indeterminateProgressDialog(R.string.update_finding_latest) {
            val task = launch(UI) {
                try {
                    val release = async { fetchUpdateLink() }.await()

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
            val link = preferences.lastRelease ?: run {
                showMessage(R.string.update_not_found)
                return@setOnDismissListener
            }

            context.progressDialog(R.string.update_downloading) {
                setProgressNumberFormat(null)
                max = 100

                val file = UpdateFileProvider.getUpdateDirectory(context).resolve("update.apk")
                val task = launch(UI) {
                    try {
                        async {
                            URL(link).download(file, preferences.connectionTimeout) { value ->
                                launch(UI) { progress = value }
                            }
                        }.await()

                        installUpdate(file)
                        preferences.lastRelease = null
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
