package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback
import com.mikepenz.iconics.IconicsDrawable
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.database.database
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.layout.ScheduleSnapshotItem
import java.util.LinkedList
import java.util.Queue

class HistoryView(context: Context) : BaseScreenView<HistoryScreen>(context) {

    private val swipeCallback = SimpleSwipeCallback.ItemSwipeCallback { p0, p1 -> screen.itemSwiped(p0, p1) }

    private val recycler = recyclerView {
        lparams(width = matchParent, height = matchParent)
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
    }

    fun attachAdapter(adapter: RecyclerView.Adapter<*>) {
        recycler.adapter = adapter

        val color = ContextCompat.getColor(context, R.color.snapshot_remove_background_color)
        val leaveBehind = IconicsDrawable(context)
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(R.color.snapshot_remove_text_color)
                .sizeDp(24)
        val directions = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        val simpleCallback = SimpleSwipeCallback(swipeCallback, leaveBehind, directions)
                .withLeaveBehindSwipeRight(leaveBehind)
                .withBackgroundSwipeLeft(color)
                .withBackgroundSwipeRight(color)

        ItemTouchHelper(simpleCallback).attachToRecyclerView(recycler)
    }

}

class HistoryScreen(private val group: String) : Screen<HistoryView>() {

    private val adapter = ItemAdapter<ScheduleSnapshotItem>()
    private val fastAdapter: FastAdapter<ScheduleSnapshotItem> = FastAdapter.with(adapter)

    private val pendingRemoves: Queue<Long> = LinkedList()

    private var keepScreenAlive = false

    override fun createView(context: Context) = HistoryView(context).apply { attachAdapter(fastAdapter) }

    override fun onShow(context: Context) {
        super.onShow(context)
        EventBus.broadcast(Event.SET_DRAWER_ENABLED, false)

        if (keepScreenAlive) {
            keepScreenAlive = false
            return
        }

        val snapshots = activity.database.snapshots.request(group)
        adapter.set(snapshots.map {
            ScheduleSnapshotItem(it).apply {
                clickListener = { clicked ->
                    adapter.adapterItems.forEachIndexed { position, item ->
                        val selected = item == clicked

                        if (item.isSelected != selected) {
                            item.withSetSelected(selected)
                            fastAdapter.notifyAdapterItemChanged(position)
                        }
                    }
                }
                pinClickListener = {
                    pinned = !pinned

                    val position = adapter.getAdapterPosition(this)
                    fastAdapter.notifyAdapterItemChanged(position)
                }
            }
        })
    }

    override fun onSave(outState: Bundle?) {
        super.onSave(outState)
        keepScreenAlive = true
    }

    override fun onHide(context: Context) {
        if (!keepScreenAlive) {
            val database = activity.database

            while (pendingRemoves.isNotEmpty()) {
                database.snapshots.remove(pendingRemoves.poll())
            }

            adapter.adapterItems.forEach {
                database.snapshots.update(it.id, it.pinned, it.isSelected)
            }

            EventBus.broadcast(Event.SET_DRAWER_ENABLED, true)
        }

        super.onHide(context)
    }

    fun itemSwiped(position: Int, direction: Int) {
        val item = adapter.getAdapterItem(position)
        pendingRemoves += item.id

        item.withIsSwipeable(false).cancelClickListener = {
            pendingRemoves -= it.id

            it.withIsSwipeable(true).cancelClickListener = null

            // TODO: fix stuck item when task is cancelled too fast
            val newPosition = adapter.getAdapterPosition(it)
            fastAdapter.notifyAdapterItemChanged(newPosition)
        }

        fastAdapter.notifyAdapterItemChanged(position)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_history, group)!!
}
