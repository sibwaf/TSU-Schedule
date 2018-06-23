package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.database.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.ChangelogItem
import ru.dyatel.tsuschedule.layout.DIM_ELEVATION_F
import ru.dyatel.tsuschedule.layout.DIM_LARGE
import ru.dyatel.tsuschedule.updater.Release
import ru.dyatel.tsuschedule.updater.Updater
import ru.dyatel.tsuschedule.utilities.ReplacingJobLauncher
import ru.dyatel.tsuschedule.utilities.ctx
import ru.dyatel.tsuschedule.utilities.hideIf

class ChangelogView(context: Context) : BaseScreenView<ChangelogScreen>(context) {

    private companion object {
        val errorContainerViewId = View.generateViewId()
        val errorViewId = View.generateViewId()
        val recyclerViewId = View.generateViewId()
    }

    private val swipeRefresh: SwipeRefreshLayout
    private val errorContainerView: View
    private val errorView: TextView
    private val changelogRecycler: RecyclerView

    var isRefreshing: Boolean
        get() = swipeRefresh.isRefreshing
        set(value) {
            swipeRefresh.isRefreshing = value
        }

    var error: String?
        get() = errorView.text.toString().takeUnless { it.isEmpty() }
        set(value) {
            errorView.text = value
            errorContainerView.hideIf { value.isNullOrEmpty() }
        }

    init {
        swipeRefresh = swipeRefreshLayout {
            verticalLayout {
                frameLayout {
                    id = errorContainerViewId

                    lparams(width = matchParent, height = wrapContent) {
                        padding = DIM_LARGE
                    }

                    backgroundColorResource = R.color.changelog_error_background_color
                    ViewCompat.setElevation(this, DIM_ELEVATION_F)

                    textView {
                        id = errorViewId
                        gravity = Gravity.CENTER

                        textColorResource = R.color.changelog_error_text_color
                    }
                }

                recyclerView {
                    id = recyclerViewId

                    lparams(width = matchParent, height = matchParent)

                    layoutManager = LinearLayoutManager(context)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }
            }

            setOnRefreshListener { screen.fetchChangelog() }
        }

        errorContainerView = swipeRefresh.find(errorContainerViewId)
        errorView = swipeRefresh.find(errorViewId)
        changelogRecycler = swipeRefresh.find(recyclerViewId)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        changelogRecycler.adapter = adapter
    }

}

class ChangelogScreen : Screen<ChangelogView>() {

    private val adapter = ItemAdapter<ChangelogItem>()
    private val fastAdapter: FastAdapter<ChangelogItem> = FastAdapter.with(adapter)

    private val updater by lazy { Updater(activity) }

    private val task = ReplacingJobLauncher(UI)

    override fun createView(context: Context) = ChangelogView(context).apply { setAdapter(fastAdapter) }

    override fun onShow(context: Context?) {
        super.onShow(context)
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, false)

        bindChangelog()
        if (view.error != null) {
            fetchChangelog()
        }
    }

    override fun onHide(context: Context?) {
        task.cancel()

        EventBus.broadcast(Event.SET_DRAWER_ENABLED, true)
        super.onHide(context)
    }

    private fun bindChangelog() {
        val changelog = activity.database.changelogs.request()
        adapter.set(changelog.map { ChangelogItem(it) })

        view.error = when {
            changelog.none() -> {
                ctx!!.getString(R.string.changelog_error_empty)
            }
            Release(changelog.first().version) < Release.CURRENT -> {
                ctx!!.getString(R.string.changelog_error_outdated)
            }
            else -> null
        }
    }

    fun fetchChangelog() {
        task.launch {
            view.isRefreshing = true
            try {
                async {
                    updater.fetchUpdate(true)
                }.await()

                bindChangelog()
            } catch (e: Exception) {
                e.handle { message -> view?.let { longSnackbar(it, message) } }
            } finally {
                view?.isRefreshing = false
            }
        }
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_changelog)!!

}
