package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.find
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.topPadding
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.database.ExamScheduleDao
import ru.dyatel.tsuschedule.database.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.layout.DIM_CARD_VERTICAL_MARGIN
import ru.dyatel.tsuschedule.layout.ExamItem
import ru.dyatel.tsuschedule.parsing.DataRequester
import ru.dyatel.tsuschedule.parsing.ExamScheduleParser
import ru.dyatel.tsuschedule.utilities.ReplacingJobLauncher
import ru.dyatel.tsuschedule.utilities.ctx
import ru.dyatel.tsuschedule.utilities.schedulePreferences

class ExamScheduleView(context: Context) : BaseScreenView<ExamScheduleScreen>(context) {

    private companion object {
        val recyclerViewId = View.generateViewId()
    }

    private val swipeRefresh: SwipeRefreshLayout
    private val examRecycler: RecyclerView

    var isRefreshing: Boolean
        get() = swipeRefresh.isRefreshing
        set(value) {
            swipeRefresh.isRefreshing = value
        }

    init {
        swipeRefresh = swipeRefreshLayout {
            recyclerView {
                id = recyclerViewId

                lparams(width = matchParent, height = matchParent) {
                    clipToPadding = true
                    topPadding = DIM_CARD_VERTICAL_MARGIN
                    bottomPadding = DIM_CARD_VERTICAL_MARGIN
                }

                layoutManager = LinearLayoutManager(context)
                descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            }

            setOnRefreshListener { screen.fetch() }
        }

        examRecycler = swipeRefresh.find(recyclerViewId)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        examRecycler.adapter = adapter
    }

}

class ExamScheduleScreen(private val group: String) : Screen<ExamScheduleView>() {

    private lateinit var examDao: ExamScheduleDao

    private val adapter = ItemAdapter<ExamItem>()
    private val fastAdapter: FastAdapter<ExamItem> = FastAdapter.with(adapter)

    private val task = ReplacingJobLauncher(UI)

    override fun createView(context: Context) = ExamScheduleView(context).apply { setAdapter(fastAdapter) }

    override fun onShow(context: Context?) {
        super.onShow(context)
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, false)

        examDao = activity.database.exams
        adapter.set(examDao.request(group).map { ExamItem(it) })

        if (adapter.adapterItems.isEmpty()) {
            fetch()
        }
    }

    override fun onHide(context: Context?) {
        task.cancel()
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, true)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_exams, group)!!

    fun fetch() {
        val preferences = ctx!!.schedulePreferences

        task.launch {
            try {
                view.isRefreshing = true

                val exams = async {
                    val requester = DataRequester().apply { timeout = preferences.connectionTimeout }
                    val exams = ExamScheduleParser.parse(requester.examSchedule(group))

                    examDao.save(group, exams)

                    exams
                }.await()

                FastAdapterDiffUtil.set(adapter, exams.map { ExamItem(it) }, false)
            } catch (e: Exception) {
                e.handle { message -> view?.let { longSnackbar(it, message) } }
            } finally {
                view.isRefreshing = false
            }
        }
    }

}
