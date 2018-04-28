package ru.dyatel.tsuschedule.updater

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.topPadding
import ru.dyatel.tsuschedule.BadGroupException
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.database.database
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.ChangelogItem
import ru.dyatel.tsuschedule.layout.DIM_DIALOG_SIDE_PADDING
import ru.dyatel.tsuschedule.utilities.Validator
import ru.dyatel.tsuschedule.utilities.download
import ru.dyatel.tsuschedule.utilities.schedulePreferences
import java.io.File
import java.net.URL

class Updater(activity: Activity) {

    private val context: Context = activity
    private val preferences = context.schedulePreferences

    private val changelogDao = activity.database.changelogs

    private val api = UpdaterApi()

    fun fetchUpdate(): Release? {
        val allowPrerelease = preferences.allowPrerelease

        api.setTimeout(preferences.connectionTimeout)

        val releases = api.getReleases()
        changelogDao.save(releases)

        val update = releases
                .sortedByDescending { it.release }
                .firstOrNull { !it.prerelease || allowPrerelease }
                ?.release
                ?.takeIf { Release.CURRENT < it }

        preferences.lastRelease = update?.url
        return update
    }

    fun installUpdate(file: File) {
        val uri = UpdateFileProvider.getUriForFile(context, file)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, uri)
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        context.startActivity(intent)
    }

    fun handleMigration() {
        val lastUsedVersion = preferences.lastUsedVersion
                .takeUnless { it == BuildConfig.VERSION_CODE } ?: return

        if (lastUsedVersion <= 11) {
            try {
                preferences.group
                        ?.let { Validator.validateGroup(it) }
                        ?.let { preferences.addGroup(it) }
            } catch (e: BadGroupException) {
            }
        }
        preferences.lastUsedVersion = BuildConfig.VERSION_CODE

        showChangelog()
    }

    fun showChangelog() {
        val itemAdapter = ItemAdapter<ChangelogItem>()
        val fastAdapter: FastAdapter<ChangelogItem> = FastAdapter.with(itemAdapter)

        itemAdapter.set(changelogDao.request().map { ChangelogItem(it) })

        val view = context.frameLayout {
            lparams(width = matchParent) {
                topPadding = DIM_DIALOG_SIDE_PADDING
                leftPadding = DIM_DIALOG_SIDE_PADDING
                rightPadding = DIM_DIALOG_SIDE_PADDING
            }

            recyclerView {
                lparams(width = matchParent)

                layoutManager = LinearLayoutManager(context)
                adapter = fastAdapter

                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
        }

        AlertDialog.Builder(context)
                .setTitle(R.string.dialog_changelog_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_ok, { _, _ -> })
                .show()
    }

    fun checkDialog(showMessage: (Int) -> Unit = {}): ProgressDialog {
        return context.indeterminateProgressDialog(R.string.update_finding_latest) {
            val task = launch(UI) {
                try {
                    val release = async { fetchUpdate() }.await()

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
