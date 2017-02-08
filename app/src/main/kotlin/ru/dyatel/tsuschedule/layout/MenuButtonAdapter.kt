package ru.dyatel.tsuschedule.layout

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.dyatel.tsuschedule.NavigationHandler
import ru.dyatel.tsuschedule.R
import java.util.LinkedList

class MenuButtonAdapter(private val navigationHandler: NavigationHandler) : RecyclerView.Adapter<MenuButtonAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {

        val button = v as TextView

    }

    private val entries: MutableList<MenuEntry> = LinkedList()

    fun addEntry(entry: MenuEntry) {
        entries += entry
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_button, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val entry = entries[position]
        holder.button.setCompoundDrawablesWithIntrinsicBounds(entry.iconResId, 0, 0, 0)
        holder.button.setText(entry.textResId)
        holder.button.setOnClickListener {
            navigationHandler.navigate(entry.fragmentProvider.invoke(), entry.name)
        }
    }

    override fun getItemCount() = entries.size

}