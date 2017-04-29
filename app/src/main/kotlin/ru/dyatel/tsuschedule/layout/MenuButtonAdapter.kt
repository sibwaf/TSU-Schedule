package ru.dyatel.tsuschedule.layout

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import java.util.LinkedList

class MenuButtonAdapter : RecyclerView.Adapter<MenuButtonAdapter.Holder>() {

    class Holder(val button: TextView) : RecyclerView.ViewHolder(button)

    private val entries: MutableList<MenuEntry> = LinkedList()

    fun addEntry(entry: MenuEntry) {
        entries += entry
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_button, parent, false)
        return Holder(view as TextView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val entry = entries[position]
        with(holder.button) {
            setCompoundDrawablesWithIntrinsicBounds(entry.iconResId, 0, 0, 0)
            setText(entry.textResId)
            setOnClickListener { EventBus.broadcast(Event.NAVIGATE_TO, entry) }
        }
    }

    override fun getItemCount() = entries.size

}