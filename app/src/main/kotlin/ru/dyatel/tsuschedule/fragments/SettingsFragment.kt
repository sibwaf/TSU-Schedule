package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.util.Log
import com.crashlytics.android.Crashlytics
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.UpdateFileProvider
import ru.dyatel.tsuschedule.UpdateParsingException
import ru.dyatel.tsuschedule.Updater
import ru.dyatel.tsuschedule.download
import ru.dyatel.tsuschedule.schedulePreferences
import ru.dyatel.tsuschedule.setMessage
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URL

class SettingsFragment : PreferenceFragment() {

    private lateinit var updateButton: Preference
    private var updateButtonCheckingMode = true

    private val updater = Updater()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        preferenceScreen.findPreference(getString(R.string.preference_timeout))
                .onPreferenceChangeListener = NumberPreferenceValidator(constraint = 1..30)

        updateButton = preferenceScreen.findPreference(getString(R.string.preference_update)).apply {
            setOnPreferenceClickListener {
                if (updateButtonCheckingMode) checkUpdates() else installUpdate()
                true
            }
        }
        syncUpdateButton()
    }

    // TODO: do this based on event
    private fun syncUpdateButton() {
        updateButtonCheckingMode = ctx.schedulePreferences.lastRelease == null
        if (updateButtonCheckingMode)
            updateButton.setTitle(R.string.preference_update_check_title)
        else
            updateButton.setTitle(R.string.preference_update_title)

    }

    private fun checkUpdates() = doAsync {
        val failure = try {
            val release = updater.getLatestRelease()

            if (release == null || !release.isNewerThanInstalled()) {
                uiThread { longToast(R.string.update_not_found) }
                ctx.schedulePreferences.lastRelease = null
            } else {
                uiThread { longToast(R.string.update_found) }
                ctx.schedulePreferences.lastRelease = release.url
            }
            uiThread { syncUpdateButton() }

            null
        } catch (e: UpdateParsingException) {
            if (BuildConfig.DEBUG) Log.e("Updater", "Failed to parse the response", e)
            else Crashlytics.logException(e)
            R.string.failure_parsing_failed
        } catch (e: SocketTimeoutException) {
            R.string.failure_connection_timeout
        } catch (e: IOException) {
            R.string.failure_unsuccessful_request
        } catch (e: Exception) {
            uiThread { throw e }
            return@doAsync
        }

        failure?.let { uiThread { longToast(failure) } }
    }

    private fun installUpdate() {
        indeterminateProgressDialog(R.string.update_finding_latest) {
            setProgressNumberFormat(null)

            val preferences = ctx.schedulePreferences

            val downloader = doAsync {
                try {
                    val release = updater.getLatestRelease()
                    if (release == null || !release.isNewerThanInstalled()) {
                        preferences.lastRelease = null
                        uiThread {
                            longToast(R.string.update_not_found)
                            syncUpdateButton()
                            dismiss()
                        }
                        return@doAsync
                    } else {
                        preferences.lastRelease = release.url
                    }
                } catch (e: Exception) {
                }

                uiThread { setMessage(R.string.update_downloading) }

                val failure = try {
                    val file = UpdateFileProvider.getUpdateDirectory(ctx).resolve("update.apk")
                    URL(preferences.lastRelease).download(file, preferences.connectionTimeout * 1000) { value ->
                        uiThread {
                            isIndeterminate = false
                            max = 100
                            progress = value
                        }
                    }
                    updater.installUpdate(file, ctx)
                    preferences.lastRelease = null

                    null
                } catch (e: SocketTimeoutException) {
                    R.string.failure_connection_timeout
                } catch (e: IOException) {
                    R.string.failure_unsuccessful_request
                } catch (e: InterruptedException) {
                    R.string.update_cancelled
                } catch (e: Exception) {
                    uiThread { throw e }
                    return@doAsync
                } finally {
                    uiThread { dismiss() }
                }

                failure?.let { uiThread { longToast(failure) } }
            }

            setOnCancelListener { downloader.cancel(true) }
        }
    }

}

private class NumberPreferenceValidator(
        val acceptEmptyInput: Boolean = false,
        val constraint: IntRange? = null
) : Preference.OnPreferenceChangeListener {

    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        if (!acceptEmptyInput && newValue == "") return false
        val number: Int
        try {
            if (acceptEmptyInput && newValue == "") number = 0
            else number = (newValue as String).toInt()
        } catch (e: NumberFormatException) {
            return false
        }

        return constraint == null || number in constraint
    }

}
